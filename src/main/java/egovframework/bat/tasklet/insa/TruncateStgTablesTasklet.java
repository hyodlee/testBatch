package egovframework.bat.tasklet.insa;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * 스테이징 DB의 조직 및 사원 테이블을 비우는 Tasklet.
 */
public class TruncateStgTablesTasklet implements Tasklet {

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
        try (SqlSession session = sqlSessionFactory.openSession()) {
            // 조직 테이블 비우기
            session.update("Employee.truncateOrgnztInfo");
            // 사원 테이블 비우기
            session.update("Employee.truncateEmplyrInfo");
            session.commit();
        }
        return RepeatStatus.FINISHED;
    }
}
