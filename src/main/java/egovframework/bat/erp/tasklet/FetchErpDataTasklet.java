package egovframework.bat.erp.tasklet;

import egovframework.bat.erp.domain.VehicleInfo;
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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * ERP 시스템에서 차량 정보를 조회하여 STG 테이블에 적재하는 Tasklet.
 */
@Component
public class FetchErpDataTasklet implements Tasklet {

    /** 로거 */
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchErpDataTasklet.class);

    /** 외부 API 호출용 RestTemplate */
    private final RestTemplate restTemplate;

    /** 데이터 적재용 JdbcTemplate */
    private final JdbcTemplate jdbcTemplate;

    /** 장애 알림 전송기 목록 */
    private final List<NotificationSender> notificationSenders;

    /** 차량 정보를 조회할 API URL */
    @Value("${Globals.Erp.ApiUrl}")
    private String apiUrl;

    public FetchErpDataTasklet(RestTemplateBuilder restTemplateBuilder,
                               @Qualifier("jdbcTemplateLocal") JdbcTemplate jdbcTemplate,
                               List<NotificationSender> notificationSenders) {
        this.restTemplate = restTemplateBuilder.build();
        this.jdbcTemplate = jdbcTemplate;
        this.notificationSenders = notificationSenders;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LOGGER.info("ERP 차량 데이터 수집 시작");
        // 1. 외부 API 호출
        List<VehicleInfo> vehicles = fetchVehicles();
        LOGGER.info("조회된 차량 수: {}", vehicles.size());

        // 2. STG 테이블에 데이터 적재
        try {
            insertVehicles(vehicles);
        } catch (Exception e) {
            LOGGER.error("STG 테이블 적재 실패", e);
            notifyFailure("차량 데이터 적재 실패: " + e.getMessage());
            throw e;
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
                VehicleInfo[] response = restTemplate.getForObject(apiUrl, VehicleInfo[].class);
                if (response == null) {
                    LOGGER.error("ERP API 응답이 비어있음");
                    return Collections.emptyList();
                }
                return Arrays.asList(response);
            } catch (Exception e) {
                LOGGER.error("ERP API 호출 실패: 시도 {} / {}", attempt, maxAttempts, e);
                if (attempt == maxAttempts) {
                    saveFailedCall(e);
                    notifyFailure("ERP API 호출 실패: " + e.getMessage());
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * 조회된 차량 정보를 migstg 테이블에 저장한다.
     *
     * @param vehicles 차량 정보 목록
     */
    private void insertVehicles(List<VehicleInfo> vehicles) {
        String sql = "INSERT INTO migstg (customer_id, name, email, phone, reg_dttm, mod_dttm) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, vehicles, vehicles.size(), (ps, vehicle) -> {
            ps.setString(1, vehicle.getCustomerId());
            ps.setString(2, vehicle.getName());
            ps.setString(3, vehicle.getEmail());
            ps.setString(4, vehicle.getPhone());
            ps.setTimestamp(5, vehicle.getRegDttm() == null ? null : new Timestamp(vehicle.getRegDttm().getTime()));
            ps.setTimestamp(6, vehicle.getModDttm() == null ? null : new Timestamp(vehicle.getModDttm().getTime()));
        });
    }

    /**
     * 실패한 REST 호출 정보를 저장한다.
     *
     * @param e 발생한 예외
     */
    private void saveFailedCall(Exception e) {
        String sql = "INSERT INTO erp_api_fail_log (api_url, error_message, reg_dttm) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, apiUrl, e.getMessage(), new Timestamp(System.currentTimeMillis()));
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

