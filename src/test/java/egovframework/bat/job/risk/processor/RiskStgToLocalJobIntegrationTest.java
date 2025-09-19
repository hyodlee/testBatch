package egovframework.bat.job.risk.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * 리스크 STG -> 로컬 이관 잡 통합 테스트.
 */
@SpringBootTest
@SpringBatchTest
@TestPropertySource(properties = {
    "spring.batch.job.enabled=false"
})
public class RiskStgToLocalJobIntegrationTest {

    /** 스테이징 DB 접근용 JdbcTemplate */
    @Autowired
    @Qualifier("migstgJdbcTemplate")
    private JdbcTemplate migstgJdbcTemplate;

    /** 로컬 DB 접근용 JdbcTemplate */
    @Autowired
    @Qualifier("jdbcTemplateLocal")
    private JdbcTemplate jdbcTemplateLocal;

    /** 잡 실행 유틸리티 */
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    /** 테스트 대상 잡 */
    @Autowired
    @Qualifier("riskStgToLocalJob")
    private Job riskStgToLocalJob;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(riskStgToLocalJob);

        // STG 테이블 생성 및 초기화
        migstgJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS RISK_CATEGORY (" +
                "CATEGORY_ID VARCHAR(20) PRIMARY KEY, " +
                "CATEGORY_NAME VARCHAR(100), " +
                "CATEGORY_DESC VARCHAR(255))");
        migstgJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS RISK_INCIDENT (" +
                "INCIDENT_ID VARCHAR(20), " +
                "INCIDENT_NO VARCHAR(30) PRIMARY KEY, " +
                "CATEGORY_ID VARCHAR(20), " +
                "TITLE VARCHAR(200), " +
                "RISK_LEVEL VARCHAR(20), " +
                "STATUS VARCHAR(20), " +
                "OWNER_ID VARCHAR(50), " +
                "DESCRIPTION VARCHAR(4000), " +
                "OCCURRED_AT TIMESTAMP, " +
                "UPDATED_AT TIMESTAMP)");
        migstgJdbcTemplate.execute("TRUNCATE TABLE RISK_CATEGORY");
        migstgJdbcTemplate.execute("TRUNCATE TABLE RISK_INCIDENT");

        // STG 데이터 입력
        migstgJdbcTemplate.update("INSERT INTO RISK_CATEGORY (CATEGORY_ID, CATEGORY_NAME, CATEGORY_DESC) VALUES ('C1','위험관리','핵심 카테고리')");
        migstgJdbcTemplate.update("INSERT INTO RISK_CATEGORY (CATEGORY_ID, CATEGORY_NAME, CATEGORY_DESC) VALUES ('C2','정보보안','신규 카테고리')");
        migstgJdbcTemplate.update("INSERT INTO RISK_INCIDENT (INCIDENT_ID, INCIDENT_NO, CATEGORY_ID, TITLE, RISK_LEVEL, STATUS, OWNER_ID, DESCRIPTION, OCCURRED_AT, UPDATED_AT) " +
                "VALUES ('RSK0000001','INC-001','C1','중요 사고','HIGH','OPEN','manager','세부 내용',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
        migstgJdbcTemplate.update("INSERT INTO RISK_INCIDENT (INCIDENT_ID, INCIDENT_NO, CATEGORY_ID, TITLE, RISK_LEVEL, STATUS, OWNER_ID, DESCRIPTION, OCCURRED_AT, UPDATED_AT) " +
                "VALUES (NULL,'INC-002','C2','신규 이슈','MEDIUM','NEW','analyst','신규 세부 내용',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");

        // 로컬 테이블 생성 및 초기화
        jdbcTemplateLocal.execute("CREATE TABLE IF NOT EXISTS RISK_CATEGORY (" +
                "CATEGORY_ID VARCHAR(20) PRIMARY KEY, " +
                "CATEGORY_NAME VARCHAR(100), " +
                "CATEGORY_DESC VARCHAR(255))");
        jdbcTemplateLocal.execute("CREATE TABLE IF NOT EXISTS RISK_INCIDENT (" +
                "INCIDENT_ID VARCHAR(20) PRIMARY KEY, " +
                "INCIDENT_NO VARCHAR(30) UNIQUE, " +
                "CATEGORY_ID VARCHAR(20), " +
                "TITLE VARCHAR(200), " +
                "RISK_LEVEL VARCHAR(20), " +
                "STATUS VARCHAR(20), " +
                "OWNER_ID VARCHAR(50), " +
                "DESCRIPTION VARCHAR(4000), " +
                "OCCURRED_AT TIMESTAMP, " +
                "UPDATED_AT TIMESTAMP)");
        jdbcTemplateLocal.execute("TRUNCATE TABLE RISK_CATEGORY");
        jdbcTemplateLocal.execute("TRUNCATE TABLE RISK_INCIDENT");

        jdbcTemplateLocal.update("INSERT INTO RISK_CATEGORY (CATEGORY_ID, CATEGORY_NAME, CATEGORY_DESC) VALUES ('C1','구분 필요','이전 설명')");
        jdbcTemplateLocal.update("INSERT INTO RISK_INCIDENT (INCIDENT_ID, INCIDENT_NO, CATEGORY_ID, TITLE, RISK_LEVEL, STATUS, OWNER_ID, DESCRIPTION, OCCURRED_AT, UPDATED_AT) " +
                "VALUES ('RSK0000001','INC-001','C1','이전 제목','LOW','CLOSED','user1','과거 내용',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
    }

    @Test
    void stgToLocalJob_syncsRiskData() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // 카테고리 업데이트 확인
        Map<String, Object> category = jdbcTemplateLocal.queryForMap(
                "SELECT CATEGORY_NAME, CATEGORY_DESC FROM RISK_CATEGORY WHERE CATEGORY_ID='C1'");
        assertThat(category.get("CATEGORY_NAME")).isEqualTo("위험관리");
        assertThat(category.get("CATEGORY_DESC")).isEqualTo("핵심 카테고리");

        // 신규 카테고리 삽입 확인
        Map<String, Object> newCategory = jdbcTemplateLocal.queryForMap(
                "SELECT CATEGORY_NAME FROM RISK_CATEGORY WHERE CATEGORY_ID='C2'");
        assertThat(newCategory.get("CATEGORY_NAME")).isEqualTo("정보보안");

        // 기존 사건 업데이트 확인
        Map<String, Object> incident = jdbcTemplateLocal.queryForMap(
                "SELECT TITLE, RISK_LEVEL, STATUS FROM RISK_INCIDENT WHERE INCIDENT_NO='INC-001'");
        assertThat(incident.get("TITLE")).isEqualTo("중요 사고");
        assertThat(incident.get("RISK_LEVEL")).isEqualTo("HIGH");
        assertThat(incident.get("STATUS")).isEqualTo("OPEN");

        // 신규 사건 삽입 및 ID 생성 확인
        Map<String, Object> newIncident = jdbcTemplateLocal.queryForMap(
                "SELECT INCIDENT_ID, TITLE FROM RISK_INCIDENT WHERE INCIDENT_NO='INC-002'");
        assertThat(newIncident.get("TITLE")).isEqualTo("신규 이슈");
        assertThat(newIncident.get("INCIDENT_ID")).isEqualTo("RSK0000002");
    }
}
