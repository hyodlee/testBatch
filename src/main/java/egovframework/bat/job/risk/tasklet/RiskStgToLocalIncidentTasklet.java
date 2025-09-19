package egovframework.bat.job.risk.tasklet;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * 스테이징과 로컬 DB의 리스크 사건 정보를 동기화하는 Tasklet.
 */
public class RiskStgToLocalIncidentTasklet implements Tasklet {

    /** MyBatis SqlSessionFactory */
    private SqlSessionFactory sqlSessionFactory;

    /**
     * SqlSessionFactory 주입자.
     * @param sqlSessionFactory 로컬 DB용 SqlSessionFactory
     */
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            // 기존 사건 정보 갱신
            int updateCount = session.update("riskStgToLoc.updateIncident");
            // INCIDENT_ID 시퀀스 초기화
            session.update("riskStgToLoc.initIncidentSeq");
            // 신규 사건 정보 추가
            int insertCount = session.insert("riskStgToLoc.insertIncidentIncremental");
            session.commit();

            // 처리 건수 기록
            int total = updateCount + insertCount;
            contribution.getStepExecution().setReadCount(total);
            contribution.incrementWriteCount(total);
        }
        return RepeatStatus.FINISHED;
    }
}
