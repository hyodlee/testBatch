package egovframework.bat.erp.tasklet;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * STG 차량 테이블을 비우는 Tasklet.
 */
public class TruncateErpVehicleTasklet implements Tasklet {

    /** MyBatis SqlSessionFactory */
    private SqlSessionFactory sqlSessionFactory;

    /**
     * SqlSessionFactory 주입.
     * @param sqlSessionFactory STG용 SqlSessionFactory
     */
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            // STG 차량 테이블 비우기
            session.update("Vehicle.truncateErpStg");
            session.commit();
        }
        return RepeatStatus.FINISHED;
    }
}
