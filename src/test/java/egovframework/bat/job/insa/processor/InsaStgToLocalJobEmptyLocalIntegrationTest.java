package egovframework.bat.job.insa.processor;

import static org.assertj.core.api.Assertions.assertThat;

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
 * 로컬 DB가 비어 있을 때 STG 데이터가 삽입되며
 * 첫 번째 사원의 ESNTL_ID가 LND0000001인지 검증하는 통합 테스트.
 */
@SpringBootTest
@SpringBatchTest
@TestPropertySource(properties = {
    "spring.batch.job.enabled=false"
})
public class InsaStgToLocalJobEmptyLocalIntegrationTest {

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
    @Qualifier("insaStgToLocalJob")
    private Job insaStgToLocalJob;

    /**
     * 매 테스트 실행 전 테이블을 생성하고 초기 데이터를 세팅한다.
     * 로컬 사원 테이블은 비워 둔다.
     */
    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(insaStgToLocalJob);

        // STG 테이블 초기화
        migstgJdbcTemplate.execute("DROP TABLE IF EXISTS COMTNORGNZTINFO");
        migstgJdbcTemplate.execute("DROP TABLE IF EXISTS COMTNEMPLYRINFO");
        migstgJdbcTemplate.execute("CREATE TABLE COMTNORGNZTINFO (ORGNZT_ID VARCHAR(20) PRIMARY KEY, ORGNZT_NM VARCHAR(20), ORGNZT_DC VARCHAR(100))");
        migstgJdbcTemplate.execute("CREATE TABLE COMTNEMPLYRINFO (ESNTL_ID VARCHAR(20), EMPLYR_ID VARCHAR(20) PRIMARY KEY, ORGNZT_ID CHAR(20), USER_NM VARCHAR(60), SEXDSTN_CODE CHAR(1), BRTHDY CHAR(20), MBTLNUM VARCHAR(20), EMAIL_ADRES VARCHAR(50), OFCPS_NM VARCHAR(60), EMPLYR_STTUS_CODE CHAR(1), REG_DTTM TIMESTAMP, MOD_DTTM TIMESTAMP)");
        migstgJdbcTemplate.update("INSERT INTO COMTNORGNZTINFO (ORGNZT_ID, ORGNZT_NM, ORGNZT_DC) VALUES ('O1','조직1','설명1')");
        migstgJdbcTemplate.update("INSERT INTO COMTNEMPLYRINFO (ESNTL_ID, EMPLYR_ID, ORGNZT_ID, USER_NM, REG_DTTM, MOD_DTTM) VALUES ('LND0000001','emp1','O1','홍길동',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
        migstgJdbcTemplate.update("INSERT INTO COMTNEMPLYRINFO (ESNTL_ID, EMPLYR_ID, ORGNZT_ID, USER_NM, REG_DTTM, MOD_DTTM) VALUES (NULL,'emp2','O1','새사원1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
        migstgJdbcTemplate.update("INSERT INTO COMTNEMPLYRINFO (ESNTL_ID, EMPLYR_ID, ORGNZT_ID, USER_NM, REG_DTTM, MOD_DTTM) VALUES (NULL,'emp3','O1','새사원2',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");

        // 로컬 테이블 초기화 (사원 정보는 비워둔다)
        jdbcTemplateLocal.execute("DROP TABLE IF EXISTS COMTNORGNZTINFO");
        jdbcTemplateLocal.execute("DROP TABLE IF EXISTS COMTNEMPLYRINFO");
        jdbcTemplateLocal.execute("CREATE TABLE COMTNORGNZTINFO (ORGNZT_ID VARCHAR(20) PRIMARY KEY, ORGNZT_NM VARCHAR(20), ORGNZT_DC VARCHAR(100))");
        jdbcTemplateLocal.execute("CREATE TABLE COMTNEMPLYRINFO (ESNTL_ID VARCHAR(20) PRIMARY KEY, EMPLYR_ID VARCHAR(20), ORGNZT_ID CHAR(20), USER_NM VARCHAR(60), SEXDSTN_CODE CHAR(1), BRTHDY CHAR(20), MBTLNUM VARCHAR(20), EMAIL_ADRES VARCHAR(50), OFCPS_NM VARCHAR(60), EMPLYR_STTUS_CODE CHAR(1), REG_DTTM TIMESTAMP, MOD_DTTM TIMESTAMP)");
        jdbcTemplateLocal.update("INSERT INTO COMTNORGNZTINFO (ORGNZT_ID, ORGNZT_NM, ORGNZT_DC) VALUES ('O1','오래된조직','설명')");
    }

    /**
     * 로컬이 비어있는 상태에서 첫 번째 사원의 ESNTL_ID가 LND0000001로 생성되는지 확인한다.
     */
    @Test
    void stgToLocalJob_insertsFirstEmployeeStartingFromOne() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        String firstId = jdbcTemplateLocal.queryForObject(
            "SELECT ESNTL_ID FROM COMTNEMPLYRINFO ORDER BY ESNTL_ID LIMIT 1",
            String.class);
        assertThat(firstId).isEqualTo("LND0000001");
    }
}
