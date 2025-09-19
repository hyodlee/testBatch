package egovframework.bat.job.risk.tasklet;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * 리스크 도메인의 스테이징 테이블을 초기화하는 Tasklet.
 */
public class TruncateRiskStgTablesTasklet implements Tasklet {

    /** MyBatis SqlSessionFactory */
    private SqlSessionFactory sqlSessionFactory;

    /**
     * SqlSessionFactory 주입자.
     * @param sqlSessionFactory STG용 SqlSessionFactory
     */
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            // 리스크 사건 테이블 비우기
            session.update("riskRemToStg.truncateRiskIncident");
            // 리스크 카테고리 테이블 비우기
            session.update("riskRemToStg.truncateRiskCategory");
            session.commit();
        }
        return RepeatStatus.FINISHED;
    }
}
