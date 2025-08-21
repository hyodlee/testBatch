package egovframework.bat.crm.tasklet;

import egovframework.bat.crm.domain.CustomerInfo;
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
 * CRM 시스템에서 고객 정보를 조회하여 STG 테이블에 적재하는 Tasklet.
 */
@Component
public class FetchCrmDataTasklet implements Tasklet {

    /** 로거 */
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchCrmDataTasklet.class);

    /** 외부 API 호출용 RestTemplate */
    private final RestTemplate restTemplate;

    /** 데이터 적재용 JdbcTemplate */
    private final JdbcTemplate jdbcTemplate;

    /** 장애 알림 전송기 목록 */
    private final List<NotificationSender> notificationSenders;

    /** 고객 정보를 조회할 API URL */
    @Value("${crm.api.url}")
    private String apiUrl;

    public FetchCrmDataTasklet(RestTemplateBuilder restTemplateBuilder,
                               @Qualifier("jdbcTemplateLocal") JdbcTemplate jdbcTemplate,
                               List<NotificationSender> notificationSenders) {
        this.restTemplate = restTemplateBuilder.build();
        this.jdbcTemplate = jdbcTemplate;
        this.notificationSenders = notificationSenders;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LOGGER.info("CRM 고객 데이터 수집 시작");
        // 1. 외부 API 호출
        List<CustomerInfo> customers = fetchCustomers();
        LOGGER.info("조회된 고객 수: {}", customers.size());

        // 2. STG 테이블에 데이터 적재
        try {
            insertCustomers(customers);
        } catch (Exception e) {
            LOGGER.error("STG 테이블 적재 실패", e);
            notifyFailure("고객 데이터 적재 실패: " + e.getMessage());
            throw e;
        }

        LOGGER.info("CRM 고객 데이터 수집 완료");
        return RepeatStatus.FINISHED;
    }

    /**
     * CRM API를 호출하여 고객 목록을 조회한다.
     * JSON/XML 응답은 CustomerInfo 배열로 자동 매핑된다.
     * 
     * @return 고객 정보 목록
     */
    private List<CustomerInfo> fetchCustomers() {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                LOGGER.info("CRM API 호출 시도: {} / {}", attempt, maxAttempts);
                CustomerInfo[] response = restTemplate.getForObject(apiUrl, CustomerInfo[].class);
                if (response == null) {
                    LOGGER.error("CRM API 응답이 비어있음");
                    return Collections.emptyList();
                }
                return Arrays.asList(response);
            } catch (Exception e) {
                LOGGER.error("CRM API 호출 실패: 시도 {} / {}", attempt, maxAttempts, e);
                if (attempt == maxAttempts) {
                    saveFailedCall(e);
                    notifyFailure("CRM API 호출 실패: " + e.getMessage());
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * 조회된 고객 정보를 migstg 테이블에 저장한다.
     * 
     * @param customers 고객 정보 목록
     */
    private void insertCustomers(List<CustomerInfo> customers) {
        String sql = "INSERT INTO migstg (customer_id, name, email, phone, reg_dttm, mod_dttm) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, customers, customers.size(), (ps, customer) -> {
            ps.setString(1, customer.getCustomerId());
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPhone());
            ps.setTimestamp(5, customer.getRegDttm() == null ? null : new Timestamp(customer.getRegDttm().getTime()));
            ps.setTimestamp(6, customer.getModDttm() == null ? null : new Timestamp(customer.getModDttm().getTime()));
        });
    }

    /**
     * 실패한 REST 호출 정보를 저장한다.
     *
     * @param e 발생한 예외
     */
    private void saveFailedCall(Exception e) {
        String sql = "INSERT INTO crm_api_fail_log (api_url, error_message, reg_dttm) VALUES (?, ?, ?)";
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

