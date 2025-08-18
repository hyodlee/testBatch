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

