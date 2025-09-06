package egovframework.bat.job.insa.processor;

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
 * STG와 로컬 DB 간 동기화 배치의 통합 테스트.
 *
 * <p>사전 데이터 세팅 후 배치를 실행하여 갱신/삽입 결과와
 * MySQL 변수 기반 ESNTL_ID 생성이 정상 동작하는지 검증한다.</p>
 */
@SpringBootTest
@SpringBatchTest
@TestPropertySource(properties = {
    "spring.batch.job.enabled=false"
})
public class InsaStgToLocalJobIntegrationTest {

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
     * 매 테스트 실행 전 테이블을 생성하고 초기 데이터를 입력한다.
     */
    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(insaStgToLocalJob);

        // STG 테이블 초기화
        migstgJdbcTemplate.execute("TRUNCATE TABLE COMTNORGNZTINFO");
        migstgJdbcTemplate.execute("TRUNCATE TABLE COMTNEMPLYRINFO");
        migstgJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS COMTNORGNZTINFO (ORGNZT_ID VARCHAR(20) PRIMARY KEY, ORGNZT_NM VARCHAR(20), ORGNZT_DC VARCHAR(100))");
        migstgJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS COMTNEMPLYRINFO (ESNTL_ID VARCHAR(20), EMPLYR_ID VARCHAR(20) PRIMARY KEY, ORGNZT_ID CHAR(20), USER_NM VARCHAR(60), SEXDSTN_CODE CHAR(1), BRTHDY CHAR(20), MBTLNUM VARCHAR(20), EMAIL_ADRES VARCHAR(50), OFCPS_NM VARCHAR(60), EMPLYR_STTUS_CODE CHAR(1), REG_DTTM TIMESTAMP, MOD_DTTM TIMESTAMP)");
        migstgJdbcTemplate.update("INSERT INTO COMTNORGNZTINFO (ORGNZT_ID, ORGNZT_NM, ORGNZT_DC) VALUES ('O1','조직1','설명1')");
        migstgJdbcTemplate.update("INSERT INTO COMTNEMPLYRINFO (ESNTL_ID, EMPLYR_ID, ORGNZT_ID, USER_NM, REG_DTTM, MOD_DTTM) VALUES ('LND0000001','emp1','O1','홍길동',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
        migstgJdbcTemplate.update("INSERT INTO COMTNEMPLYRINFO (ESNTL_ID, EMPLYR_ID, ORGNZT_ID, USER_NM, REG_DTTM, MOD_DTTM) VALUES (NULL,'emp2','O1','새사원1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
        migstgJdbcTemplate.update("INSERT INTO COMTNEMPLYRINFO (ESNTL_ID, EMPLYR_ID, ORGNZT_ID, USER_NM, REG_DTTM, MOD_DTTM) VALUES (NULL,'emp3','O1','새사원2',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");

        // 로컬 테이블 초기화
        jdbcTemplateLocal.execute("TRUNCATE TABLE COMTNORGNZTINFO");
        jdbcTemplateLocal.execute("TRUNCATE TABLE COMTNEMPLYRINFO");
        jdbcTemplateLocal.execute("CREATE TABLE IF NOT EXISTS COMTNORGNZTINFO (ORGNZT_ID VARCHAR(20) PRIMARY KEY, ORGNZT_NM VARCHAR(20), ORGNZT_DC VARCHAR(100))");
        jdbcTemplateLocal.execute("CREATE TABLE IF NOT EXISTS COMTNEMPLYRINFO (ESNTL_ID VARCHAR(20) PRIMARY KEY, EMPLYR_ID VARCHAR(20), ORGNZT_ID CHAR(20), USER_NM VARCHAR(60), SEXDSTN_CODE CHAR(1), BRTHDY CHAR(20), MBTLNUM VARCHAR(20), EMAIL_ADRES VARCHAR(50), OFCPS_NM VARCHAR(60), EMPLYR_STTUS_CODE CHAR(1), REG_DTTM TIMESTAMP, MOD_DTTM TIMESTAMP)");
        jdbcTemplateLocal.update("INSERT INTO COMTNORGNZTINFO (ORGNZT_ID, ORGNZT_NM, ORGNZT_DC) VALUES ('O1','오래된조직','설명')");
        jdbcTemplateLocal.update("INSERT INTO COMTNEMPLYRINFO (ESNTL_ID, EMPLYR_ID, ORGNZT_ID, USER_NM, REG_DTTM, MOD_DTTM) VALUES ('LND0000001','emp1','O1','기존이름',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
    }

    /**
     * STG -> 로컬 이관 배치가 사원 정보를 갱신/삽입하고
     * MySQL 변수 기반 ID 생성이 정상적으로 이루어지는지 검증한다.
     */
    @Test
    void stgToLocalJob_updatesAndInsertsEmployees() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // 기존 사원 업데이트 확인
        String updatedName = jdbcTemplateLocal.queryForObject(
            "SELECT USER_NM FROM COMTNEMPLYRINFO WHERE EMPLYR_ID='emp1'",
            String.class);
        assertThat(updatedName).isEqualTo("홍길동");

        // 신규 사원1 삽입 및 ID 생성 확인
        Map<String, Object> emp2 = jdbcTemplateLocal.queryForMap(
            "SELECT ESNTL_ID, USER_NM FROM COMTNEMPLYRINFO WHERE EMPLYR_ID='emp2'");
        assertThat(emp2.get("USER_NM")).isEqualTo("새사원1");
        assertThat(emp2.get("ESNTL_ID")).isEqualTo("LND0000002");

        // 신규 사원2 삽입 및 ID 생성 확인
        Map<String, Object> emp3 = jdbcTemplateLocal.queryForMap(
            "SELECT ESNTL_ID, USER_NM FROM COMTNEMPLYRINFO WHERE EMPLYR_ID='emp3'");
        assertThat(emp3.get("USER_NM")).isEqualTo("새사원2");
        assertThat(emp3.get("ESNTL_ID")).isEqualTo("LND0000003");
    }
}
