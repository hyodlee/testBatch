package egovframework.bat.domain.insa;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * COMTNEMPLYRINFO 테이블의 최대 ESNTL_ID를 조회하여 새로운 ID를 생성한다.
 */
@Component
public class EsntlIdGenerator {

    /** 로거 */
    private static final Logger LOGGER = LoggerFactory.getLogger(EsntlIdGenerator.class);

    /** 로컬 DB 접근용 JdbcTemplate */
    private final JdbcTemplate jdbcTemplate;

    // 생성자 주입 시 Qualifier를 명시
    public EsntlIdGenerator(@Qualifier("jdbcTemplateLocal") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 빈 초기화 시 데이터소스 URL 확인용 로그를 출력한다.
     */
    @PostConstruct
    public void logDataSourceUrl() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            String url = connection.getMetaData().getURL();
            LOGGER.info("사용 중인 데이터소스 URL: {}", url);
        } catch (SQLException e) {
            LOGGER.error("데이터소스 URL 조회 실패", e);
        }
    }

    /**
     * 주어진 프리픽스로 시작하는 ESNTL_ID의 최대값을 조회하여 다음 번호를 생성한다.
     *
     * @param prefix ESNTL_ID에 붙일 프리픽스
     * @return 새로 생성된 ESNTL_ID
     */
    public String generate(String prefix) {
        String maxId = jdbcTemplate.queryForObject(
            "SELECT MAX(ESNTL_ID) FROM COMTNEMPLYRINFO WHERE ESNTL_ID LIKE ?",
            String.class,
            prefix + "%");

        long nextNo = 1L;
        if (maxId != null) {
            String numberPart = maxId.substring(prefix.length());
            try {
                nextNo = Long.parseLong(numberPart) + 1;
            } catch (NumberFormatException e) {
                nextNo = 1L;
            }
        }
        return prefix + nextNo;
    }
}

