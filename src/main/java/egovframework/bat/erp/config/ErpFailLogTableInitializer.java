package egovframework.bat.erp.config;

import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * ERP 연동 실패 로그 테이블을 초기화하는 컴포넌트.
 * 존재하지 않을 경우 테이블을 생성한다.
 */
@Component
public class ErpFailLogTableInitializer {

    /** 로거 */
    private static final Logger LOGGER = LoggerFactory.getLogger(ErpFailLogTableInitializer.class);

    /** 로컬 DB 연동용 JdbcTemplate */
    private final JdbcTemplate jdbcTemplate;

    public ErpFailLogTableInitializer(@Qualifier("jdbcTemplateLocal") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 애플리케이션 시작 시 실패 로그 테이블을 생성한다.
     */
    @PostConstruct
    public void createFailLogTables() {
        if (jdbcTemplate == null) {
            // DataSource가 준비되지 않은 경우에는 로그만 남기고 종료
            LOGGER.warn("jdbcTemplate이 준비되지 않아 실패 로그 테이블을 생성하지 않습니다.");
            return;
        }
        try {
            // ERP API 실패 로그 테이블 생성
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS migstg.erp_api_fail_log (" +
                "FAIL_LOG_ID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "API_URL VARCHAR(500) NOT NULL," +
                "ERROR_MESSAGE VARCHAR(1000)," +
                "REG_DTTM DATETIME NOT NULL," +
                "KEY ERP_API_FAIL_LOG_i01 (API_URL)" +
                ") ENGINE=InnoDB");

            // ERP DB 적재 실패 로그 테이블 생성
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS migstg.erp_db_fail_log (" +
                "FAIL_LOG_ID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "ERROR_MESSAGE VARCHAR(1000)," +
                "REG_DTTM DATETIME NOT NULL" +
                ") ENGINE=InnoDB");
        } catch (Exception e) {
            // 예외 발생 시 로그만 남기고 메서드를 종료하여 애플리케이션에 영향을 주지 않도록 함
            LOGGER.error("실패 로그 테이블 생성 중 오류", e);
        }
    }
}
