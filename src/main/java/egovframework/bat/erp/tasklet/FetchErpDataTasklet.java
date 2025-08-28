package egovframework.bat.erp.tasklet;

import egovframework.bat.erp.domain.VehicleInfo;
import egovframework.bat.erp.exception.ErpApiException;
import egovframework.bat.notification.NotificationSender;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * ERP 시스템에서 차량 정보를 조회하여 STG 테이블에 적재하는 Tasklet.
 */
@Component
public class FetchErpDataTasklet implements Tasklet {

    /** 로거 */
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchErpDataTasklet.class);

    /** 비동기 호출을 위한 WebClient */
    private final WebClient webClient;

    /** 데이터 적재용 JdbcTemplate */
    private final JdbcTemplate jdbcTemplate;

    /** 장애 알림 전송기 목록 */
    private final List<NotificationSender> notificationSenders;

    /** 차량 정보를 조회할 API URL */
    private final String apiUrl;

    public FetchErpDataTasklet(WebClient.Builder builder,
                               @Qualifier("jdbcTemplateLocal") JdbcTemplate jdbcTemplate,
                               List<NotificationSender> notificationSenders,
                               @Value("${erp.api-url}") String apiUrl) {
        // WebClient 생성
        this.webClient = builder.build();
        this.jdbcTemplate = jdbcTemplate;
        this.notificationSenders = notificationSenders;
        this.apiUrl = apiUrl;
    }

    /**
     * 현재 설정된 API URL을 반환한다.
     *
     * @return API URL
     */
    public String getApiUrl() {
        return apiUrl;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LOGGER.info("ERP 차량 데이터 수집 시작");
        // 1. 외부 API 호출
        List<VehicleInfo> vehicles = fetchVehicles();
        LOGGER.info("조회된 차량 수: {}", vehicles.size());

        // 조회 결과가 없으면 경고 로그만 남기고 종료
        if (vehicles.isEmpty()) {
            LOGGER.warn("조회된 차량이 없어 작업을 종료합니다.");
            return RepeatStatus.FINISHED;
        }

        // 2. STG 테이블에 데이터 적재
        try {
            insertVehicles(vehicles);
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("DB 커넥션 획득 실패", e);
        } catch (Exception e) {
            LOGGER.error("STG 테이블 적재 실패", e);
            // DB 적재 실패 로그 저장
            saveDbFail(e);
            notifyFailure("차량 데이터 적재 실패: " + e.getMessage());
        }

        LOGGER.info("ERP 차량 데이터 수집 완료");
        return RepeatStatus.FINISHED;
    }

    /**
     * ERP API를 호출하여 차량 목록을 조회한다.
     * JSON/XML 응답은 VehicleInfo 배열로 자동 매핑된다.
     *
     * @return 차량 정보 목록
     */
    private List<VehicleInfo> fetchVehicles() {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                LOGGER.info("ERP API 호출 시도: {} / {}", attempt, maxAttempts);
                // WebClient를 사용하여 ERP API 호출
                VehicleInfo[] response = webClient.get()
                        .uri(apiUrl)
                        .retrieve()
                        .bodyToMono(VehicleInfo[].class)
                        .block(); // 배치 특성상 동기 처리
                if (response == null) {
                    LOGGER.error("ERP API 응답이 비어있음");
                    return Collections.emptyList();
                }
                return Arrays.asList(response);
            } catch (WebClientResponseException.NotFound e) {
                // 404 오류는 재시도 후에도 실패하면 빈 목록 반환
                LOGGER.error("ERP API 404 오류: {}", e.getMessage(), e);
                if (attempt == maxAttempts) {
                    return Collections.emptyList();
                }
            } catch (WebClientResponseException e) {
                // 기타 HTTP 오류는 재시도 후 사용자 정의 예외로 변환
                LOGGER.error("ERP API HTTP 오류: 시도 {} / {} - 상태코드 {}", attempt, maxAttempts, e.getStatusCode(), e);
                if (attempt == maxAttempts) {
                    saveFailLog(e);
                    notifyFailure("ERP API HTTP 오류: " + e.getMessage());
                    throw new ErpApiException("ERP API 호출 실패", e);
                }
            } catch (Exception e) {
                // 네트워크 등 일반 예외 처리
                LOGGER.error("ERP API 호출 실패: 시도 {} / {}", attempt, maxAttempts, e);
                if (attempt == maxAttempts) {
                    saveFailLog(e);
                    notifyFailure("ERP API 호출 실패: " + e.getMessage());
                    throw new ErpApiException("ERP API 호출 중 예외 발생", e);
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * 조회된 차량 정보를 migstg.erp_vehicle 테이블에 저장한다.
     *
     * @param vehicles 차량 정보 목록
     */
    private void insertVehicles(List<VehicleInfo> vehicles) {
        // 빈 목록이면 DB 작업을 수행하지 않음
        if (vehicles == null || vehicles.isEmpty()) {
            LOGGER.warn("적재할 차량 정보가 없어 insertVehicles를 종료합니다.");
            return;
        }

        String sql = "INSERT INTO migstg.erp_vehicle (VEHICLE_ID, MODEL, MANUFACTURER, PRICE, REG_DTTM, MOD_DTTM) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            jdbcTemplate.batchUpdate(sql, vehicles, vehicles.size(), (ps, vehicle) -> {
                // 차량 ID
                ps.setString(1, vehicle.getVehicleId());
                // 모델명
                ps.setString(2, vehicle.getModel());
                // 제조사
                ps.setString(3, vehicle.getManufacturer());
                // 가격
                ps.setBigDecimal(4, vehicle.getPrice());
                // 등록일시
                ps.setTimestamp(5, vehicle.getRegDttm() == null ? null : new Timestamp(vehicle.getRegDttm().getTime()));
                // 수정일시
                ps.setTimestamp(6, vehicle.getModDttm() == null ? null : new Timestamp(vehicle.getModDttm().getTime()));
            });
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("차량 정보 적재 중 커넥션 획득 실패", e);
            // DB 적재 실패 로그 저장
            saveDbFail(e);
        }
    }

    /**
     * 공통 로그 저장 메서드.
     *
     * @param table  저장할 테이블명
     * @param apiUrl API URL (필요 없으면 null)
     * @param e      발생한 예외
     */
    private void saveLog(String table, String apiUrl, Exception e) {
        String sql;
        Object[] params;
        if (apiUrl != null) {
            sql = "INSERT INTO " + table + " (api_url, error_message, reg_dttm) VALUES (?, ?, ?)";
            params = new Object[] {apiUrl, e.getMessage(), new Timestamp(System.currentTimeMillis())};
        } else {
            sql = "INSERT INTO " + table + " (error_message, reg_dttm) VALUES (?, ?)";
            params = new Object[] {e.getMessage(), new Timestamp(System.currentTimeMillis())};
        }

        try {
            jdbcTemplate.update(sql, params);
        } catch (CannotGetJdbcConnectionException ex) {
            LOGGER.error("{} 로그 저장 중 커넥션 획득 실패", table, ex);
        } catch (BadSqlGrammarException ex) {
            // 테이블 미존재 등 SQL 문법 오류가 발생해도 배치를 중단하지 않음
            LOGGER.error("{} 로그 테이블 SQL 오류", table, ex);
        } catch (DataAccessException ex) {
            // 기타 데이터베이스 접근 오류 처리
            LOGGER.error("{} 로그 저장 중 데이터 접근 오류", table, ex);
        }
    }

    /**
     * 공통 로그 저장 메서드(API URL 없이).
     *
     * @param table 저장할 테이블명
     * @param e     발생한 예외
     */
    private void saveLog(String table, Exception e) {
        saveLog(table, null, e);
    }

    /**
     * API 실패 로그를 저장한다.
     *
     * @param e 발생한 예외
     */
    private void saveFailLog(Exception e) {
        saveLog("migstg.erp_api_fail_log", apiUrl, e);
    }

    /**
     * DB 적재 실패 로그를 저장한다.
     *
     * @param e 발생한 예외
     */
    private void saveDbFail(Exception e) {
        saveLog("migstg.erp_db_fail_log", e);
    }

    /**
     * 장애 알림을 전송한다.
     *
     * @param message 전송할 메시지
     */
    private void notifyFailure(String message) {
        for (NotificationSender sender : notificationSenders) {
            try {
                sender.send(message);
            } catch (Exception e) {
                LOGGER.error("알림 전송 실패", e);
            }
        }
    }
}

