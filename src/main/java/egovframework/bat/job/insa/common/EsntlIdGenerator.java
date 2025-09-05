package egovframework.bat.job.insa.common;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * COMTNEMPLYRINFO 테이블의 최대 ESNTL_ID를 조회하여 새로운 ID를 생성한다.
 */
@Component
public class EsntlIdGenerator {

    /** 로거 */
    private static final Logger LOGGER = LoggerFactory.getLogger(EsntlIdGenerator.class);

    /** 로컬 DB 접근용 JdbcTemplate */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 프리픽스별 현재 증가 번호를 보관하기 위한 맵
     * 하나의 배치 실행 동안 DB 조회는 최초 한 번만 수행한다.
     */
    private final ConcurrentMap<String, Long> prefixCounters = new ConcurrentHashMap<>();

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
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("데이터베이스 커넥션 획득 실패", e);
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
        long nextNo = prefixCounters.compute(prefix, (key, current) -> {
            if (current == null) {
                String maxId = null;
                try {
                    maxId = jdbcTemplate.queryForObject(
                        "SELECT MAX(ESNTL_ID) FROM COMTNEMPLYRINFO WHERE ESNTL_ID LIKE ?",
                        String.class,
                        prefix + "%");
                } catch (CannotGetJdbcConnectionException e) {
                    LOGGER.error("ESNTL_ID 최대값 조회 실패", e);
                }
                long start = 0L;
                if (maxId != null) {
                    String numberPart = maxId.substring(prefix.length());
                    try {
                        start = Long.parseLong(numberPart);
                    } catch (NumberFormatException e) {
                        start = 0L;
                    }
                }
                return start + 1;
            }
            return current + 1;
        });

        // ESNTL_ID 숫자 부분을 7자리로 0으로 패딩한다.
        String formattedNumber = String.format("%07d", nextNo);
        // 프리픽스와 포맷된 숫자를 결합하여 최종 ESNTL_ID를 생성한다.
        return prefix + formattedNumber;
    }
}

