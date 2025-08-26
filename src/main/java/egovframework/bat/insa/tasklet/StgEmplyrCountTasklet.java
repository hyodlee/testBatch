package egovframework.bat.insa.tasklet;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 스테이징 DB의 COMTNEMPLYRINFO 테이블 건수를 로그로 출력하는 Tasklet.
 */
public class StgEmplyrCountTasklet implements Tasklet {

    /** 로거 */
    private static final Logger LOGGER = LoggerFactory.getLogger(StgEmplyrCountTasklet.class);

    /** MyBatis SqlSessionFactory */
    private SqlSessionFactory sqlSessionFactory;

    /**
     * SqlSessionFactory 주입
     * @param sqlSessionFactory STG용 SqlSessionFactory
     */
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String sql = "SELECT COUNT(*) FROM COMTNEMPLYRINFO";
        try (SqlSession session = sqlSessionFactory.openSession();
             Connection conn = session.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt(1);
                LOGGER.info("COMTNEMPLYRINFO 현재 건수: {}", count);
            }
        }
        return RepeatStatus.FINISHED;
    }
}
