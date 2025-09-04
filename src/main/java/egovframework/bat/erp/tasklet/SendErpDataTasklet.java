package egovframework.bat.erp.tasklet;

import egovframework.bat.erp.domain.VehicleInfo;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 로컬 DB의 ERP 데이터를 외부 REST API로 전송하는 Tasklet.
 */
@Component
public class SendErpDataTasklet implements Tasklet {

    /** 로거 */
    private static final Logger LOGGER = LoggerFactory.getLogger(SendErpDataTasklet.class);

    /** 로컬 DB 접근용 JdbcTemplate */
    private final JdbcTemplate jdbcTemplate;

    /** 비동기 호출을 위한 WebClient */
    private final WebClient webClient;

    /** 전송 대상 ERP API URL */
    private final String apiUrl;

    public SendErpDataTasklet(
            @Qualifier("jdbcTemplateLocal") JdbcTemplate jdbcTemplate,
            WebClient.Builder builder,
            @Value("${erp.api-url}") String apiUrl) {
        this.jdbcTemplate = jdbcTemplate;
        this.webClient = builder.build();
        this.apiUrl = apiUrl;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LOGGER.info("로컬 ERP 데이터 전송 시작");
        // 1. 로컬 DB에서 차량 정보 조회
        List<VehicleInfo> vehicles = jdbcTemplate.query(
                "SELECT VEHICLE_ID, MODEL, MANUFACTURER, PRICE, REG_DTTM, MOD_DTTM FROM miglocal.erp_vehicle",
                new BeanPropertyRowMapper<>(VehicleInfo.class)
        );
        // 2. 조회된 데이터를 외부 ERP API로 전송
        for (VehicleInfo vehicle : vehicles) {
            webClient.post()
                    .uri(apiUrl)
                    .bodyValue(vehicle)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block(); // 배치 특성상 동기 처리
        }
        LOGGER.info("총 {}건의 데이터를 전송 완료", vehicles.size());
        return RepeatStatus.FINISHED;
    }
}

