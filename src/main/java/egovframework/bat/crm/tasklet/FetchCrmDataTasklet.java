package egovframework.bat.crm.tasklet;

import egovframework.bat.crm.domain.CustomerInfo;
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

    /** 고객 정보를 조회할 API URL */
    @Value("${crm.api.url}")
    private String apiUrl;

    public FetchCrmDataTasklet(RestTemplateBuilder restTemplateBuilder,
                               @Qualifier("jdbcTemplateLocal") JdbcTemplate jdbcTemplate) {
        this.restTemplate = restTemplateBuilder.build();
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // 1. 외부 API 호출
        List<CustomerInfo> customers = fetchCustomers();
        LOGGER.info("조회된 고객 수: {}", customers.size());

        // 2. STG 테이블에 데이터 적재
        insertCustomers(customers);

        return RepeatStatus.FINISHED;
    }

    /**
     * CRM API를 호출하여 고객 목록을 조회한다.
     * JSON/XML 응답은 CustomerInfo 배열로 자동 매핑된다.
     * 
     * @return 고객 정보 목록
     */
    private List<CustomerInfo> fetchCustomers() {
        CustomerInfo[] response = restTemplate.getForObject(apiUrl, CustomerInfo[].class);
        if (response == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(response);
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
}

