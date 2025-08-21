package egovframework.bat.insa.util;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import egovframework.bat.insa.util.EsntlIdGenerator;

/**
 * ESNTL_ID 생성기가 각 레코드마다 고유한 값을 부여하는지 검증하는 테스트
 */
public class EsntlIdGeneratorTest {

    /** 로깅을 위한 로거 */
    private static final Logger LOGGER = LoggerFactory.getLogger(EsntlIdGeneratorTest.class);

    private JdbcTemplate jdbcTemplate;
    private EsntlIdGenerator generator;

    @Before
    public void setUp() {
        // H2 인메모리 DB 설정 (MySQL 모드 사용)
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1");
        ds.setUsername("sa");
        ds.setPassword("");
        jdbcTemplate = new JdbcTemplate(ds);

        // 테스트용 테이블 생성
        jdbcTemplate.execute("CREATE TABLE COMTNEMPLYRINFO (ESNTL_ID VARCHAR(20) PRIMARY KEY, EMPLYR_ID VARCHAR(20))");

        generator = new EsntlIdGenerator(jdbcTemplate);
    }

    @Test
    public void uniqueIdInsertTest() {
        // 12건의 사원 정보를 업서트로 삽입하면서 ESNTL_ID를 생성
        for (int i = 0; i < 12; i++) {
            String esntlId = generator.generate("LND");
            // 생성된 ESNTL_ID를 로깅 및 출력
            LOGGER.info("생성된 ESNTL_ID: {}", esntlId);
            System.out.println("생성된 ESNTL_ID: " + esntlId);

            jdbcTemplate.update(
                "INSERT INTO COMTNEMPLYRINFO (ESNTL_ID, EMPLYR_ID) VALUES (?, ?) ON DUPLICATE KEY UPDATE EMPLYR_ID = VALUES(EMPLYR_ID)",
                esntlId,
                "EMP" + i);
        }

        // 삽입된 ESNTL_ID 수와 고유성을 검증
        List<String> ids = jdbcTemplate.queryForList("SELECT ESNTL_ID FROM COMTNEMPLYRINFO", String.class);
        assertEquals(12, ids.size());
        Set<String> unique = new HashSet<>(ids);
        assertEquals(12, unique.size());
    }
}
