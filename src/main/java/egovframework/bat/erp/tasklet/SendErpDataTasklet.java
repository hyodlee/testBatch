package egovframework.bat.erp.tasklet;

import egovframework.bat.erp.domain.VehicleInfo;
import egovframework.bat.erp.exception.ErpApiException;
import egovframework.bat.notification.NotificationSender;
import java.util.List;
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

    public SendErpDataTasklet(WebClient.Builder builder,
                              @Qualifier("migstgJdbcTemplate") JdbcTemplate jdbcTemplate,
                              List<NotificationSender> notificationSenders,
                              @Value("${erp.outbound-api-url}") String apiUrl) {
        this.webClient = builder.build();
        this.jdbcTemplate = jdbcTemplate;
        this.notificationSenders = notificationSenders;
        this.apiUrl = apiUrl;
    }

    /** 현재 설정된 외부 API URL 반환 */
    public String getApiUrl() {
        return apiUrl;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LOGGER.info("ERP 데이터 외부 전송 시작");
        List<VehicleInfo> vehicles;
        try {
            vehicles = fetchVehicles();
        } catch (DataAccessException e) {
            LOGGER.error("STG 데이터 조회 실패", e);
            notifyFailure("STG 데이터 조회 실패: " + e.getMessage());
            throw e;
        }

        if (vehicles.isEmpty()) {
            LOGGER.info("전송할 데이터가 없습니다.");
            return RepeatStatus.FINISHED;
        }

        for (VehicleInfo vehicle : vehicles) {
            try {
                sendVehicle(vehicle);
            } catch (ErpApiException e) {
                LOGGER.error("차량 전송 실패: {}", vehicle.getVehicleId(), e);
                notifyFailure("차량 전송 실패: " + e.getMessage());
                throw e;
            }
        }

        LOGGER.info("ERP 데이터 외부 전송 완료");
        return RepeatStatus.FINISHED;
    }

    /** STG DB에서 전송 대상 차량 목록 조회 */
    private List<VehicleInfo> fetchVehicles() {
        String sql = "SELECT VEHICLE_ID, MODEL, MANUFACTURER, PRICE, REG_DTTM, MOD_DTTM FROM MIGSTG.ERP_VEHICLE";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(VehicleInfo.class));
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

