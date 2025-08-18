package egovframework.bat.domain.insa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * COMTNEMPLYRINFO 테이블의 최대 ESNTL_ID를 조회하여 새로운 ID를 생성한다.
 */
@Component
@RequiredArgsConstructor
public class EsntlIdGenerator {

    private final JdbcTemplate jdbcTemplate;

    /**
     * COMTNEMPLYRINFO에서 최대 ESNTL_ID를 조회하여 1을 더한 값을 반환한다.
     *
     * @return 새로운 ESNTL_ID
     */
    public String generate() {
        Long nextId = jdbcTemplate.queryForObject(
            "SELECT COALESCE(MAX(CAST(ESNTL_ID AS UNSIGNED)),0) + 1 FROM COMTNEMPLYRINFO",
            Long.class);
        return String.valueOf(nextId);
    }
}

