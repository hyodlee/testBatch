package egovframework.bat.job.erp.tasklet;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import egovframework.bat.job.erp.domain.VehicleInfo;
import egovframework.bat.job.erp.exception.ErpApiException;
import egovframework.bat.notification.NotificationSender;

/**
 * STG DB에 적재된 ERP 데이터를 외부 시스템으로 전송하는 Tasklet.
 */
@Component
public class SendErpDataTasklet implements Tasklet {

    /** 로거 */
    private static final Logger LOGGER = LoggerFactory.getLogger(SendErpDataTasklet.class);

    /** WebClient 인스턴스 */
    private final WebClient webClient;

    /** STG DB 접근을 위한 JdbcTemplate */
    private final JdbcTemplate jdbcTemplate;

    /** 장애 알림 전송기 목록 */
    private final List<NotificationSender> notificationSenders;

    /** 외부 ERP API URL */
    private final String apiUrl;

    /**
     * 한 번에 조회할 데이터 건수.
     * 기본값은 100이며, Job 파라미터로 "pageSize"를 전달하면 그 값을 사용한다.
     */
    private final int defaultPageSize;

    public SendErpDataTasklet(WebClient.Builder builder,
                              @Qualifier("migstgJdbcTemplate") JdbcTemplate jdbcTemplate,
                              @Qualifier("emailNotificationSender") NotificationSender emailNotificationSender,
                              @Qualifier("smsNotificationSender") NotificationSender smsNotificationSender,
                              @Value("${erp.outbound-api-url}") String apiUrl,
                              @Value("${erp.fetch-page-size:100}") int defaultPageSize) {
        this.webClient = builder.build();
        this.jdbcTemplate = jdbcTemplate;
        // 주입받은 알림 전송기를 리스트로 구성
        this.notificationSenders = List.of(emailNotificationSender, smsNotificationSender);
        this.apiUrl = apiUrl;
        this.defaultPageSize = defaultPageSize;
    }

    /** 현재 설정된 외부 API URL 반환 */
    public String getApiUrl() {
        return apiUrl;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LOGGER.info("ERP 데이터 외부 전송 시작");

        // Job 파라미터로 전달된 pageSize가 있으면 우선 적용
        Map<String, Object> params = chunkContext.getStepContext().getJobParameters();
        int pageSize = this.defaultPageSize;
        Object pageSizeParam = params.get("pageSize");
        if (pageSizeParam != null) {
            try {
                pageSize = Integer.parseInt(pageSizeParam.toString());
            } catch (NumberFormatException e) {
                LOGGER.warn("잘못된 pageSize 파라미터: {}", pageSizeParam);
            }
        }

        long lastId = 0L;
        while (true) {
            List<VehicleInfo> vehicles;
            try {
                vehicles = fetchVehicles(lastId, pageSize);
            } catch (DataAccessException e) {
                LOGGER.error("STG 데이터 조회 실패", e);
                notifyFailure("STG 데이터 조회 실패: " + e.getMessage());
                throw e;
            }

            if (vehicles.isEmpty()) {
                break;
            }

            for (VehicleInfo vehicle : vehicles) {
                try {
                    sendVehicle(vehicle);
                    lastId = vehicle.getVehicleId(); // 다음 페이지 조회를 위한 마지막 ID 갱신
                } catch (ErpApiException e) {
                    LOGGER.error("차량 전송 실패: {}", vehicle.getVehicleId(), e);
                    notifyFailure("차량 전송 실패: " + e.getMessage());
                    throw e;
                }
            }
        }

        LOGGER.info("ERP 데이터 외부 전송 완료");
        return RepeatStatus.FINISHED;
    }

    /**
     * STG DB에서 전송 대상 차량 목록 조회.
     * VEHICLE_ID를 기준으로 key-based 커서를 적용하며, 한 번에 pageSize만큼만 조회한다.
     * ResultSet의 fetchSize를 지정하여 메모리 사용량을 줄인다.
     */
    private List<VehicleInfo> fetchVehicles(long lastId, int pageSize) {
        String sql = "SELECT VEHICLE_ID, MODEL, MANUFACTURER, PRICE, REG_DTTM, MOD_DTTM "
                   + "FROM MIGSTG.ERP_VEHICLE "
                   + "WHERE VEHICLE_ID > ? ORDER BY VEHICLE_ID LIMIT ?";
        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setLong(1, lastId);
            ps.setInt(2, pageSize);
            ps.setFetchSize(pageSize); // 스트리밍 처리를 위한 fetchSize 설정
            return ps;
        }, new BeanPropertyRowMapper<>(VehicleInfo.class));
    }

    /** 단건 차량 정보를 외부 ERP API로 전송 */
    private void sendVehicle(VehicleInfo vehicle) {
        try {
            webClient.post()
                    .uri(apiUrl)
                    .bodyValue(vehicle)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new ErpApiException("ERP API 호출 실패", e);
        }
    }

    /** 장애 알림 전송 */
    private void notifyFailure(String message) {
        for (NotificationSender sender : notificationSenders) {
            try {
                sender.send(message);
            } catch (Exception e) {
                LOGGER.warn("알림 전송 중 오류 발생", e);
            }
        }
    }
}

