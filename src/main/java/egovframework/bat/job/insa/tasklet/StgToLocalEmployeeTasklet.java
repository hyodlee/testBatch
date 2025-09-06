package egovframework.bat.job.insa.tasklet;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * 스테이징 DB와 로컬 DB의 사원 정보를 동기화하는 Tasklet.
 * ESNTL_ID로 기존 로컬 데이터를 갱신하고 존재하지 않는 사원은 신규로 추가한다.
 */
public class StgToLocalEmployeeTasklet implements Tasklet {

    /** MyBatis SqlSessionFactory */
    private SqlSessionFactory sqlSessionFactory;

    /**
     * SqlSessionFactory 주입
     * @param sqlSessionFactory 로컬 DB용 SqlSessionFactory
     */
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            // 기존 사원 정보 갱신
            int updateCount = session.update("insaStgToLoc.updateEmployee");
            // 신규 사원 정보 추가
            int insertCount = session.insert("insaStgToLoc.insertEmployeeIncremental");
            session.commit();

            // 처리 건수를 스텝 컨트리뷰션에 반영
            int total = updateCount + insertCount;
            // StepExecution을 직접 사용하여 읽기 건수를 설정
            contribution.getStepExecution().setReadCount(total);
            contribution.incrementWriteCount(total);
        }
        return RepeatStatus.FINISHED;
    }
}
