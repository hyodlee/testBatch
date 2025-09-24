package egovframework.bat.job.insa.tasklet;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.util.StringUtils;

/**
 * 스테이징 DB와 로컬 DB의 사원 정보를 동기화하는 Tasklet.
 * ESNTL_ID로 기존 로컬 데이터를 갱신하고 존재하지 않는 사원은 신규로 추가한다.
 */
public class StgToLocalEmployeeTasklet implements Tasklet {

    /** MyBatis SqlSessionFactory */
    private SqlSessionFactory sqlSessionFactory;

    /** 배치 실행 파라미터: 시작일자(yyyyMMdd) */
    private String startDate;

    /** 배치 실행 파라미터: 종료일자(yyyyMMdd) */
    private String endDate;

    /**
     * SqlSessionFactory 주입
     * @param sqlSessionFactory 로컬 DB용 SqlSessionFactory
     */
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * 시작일자 파라미터 주입
     *
     * @param startDate 시작일자(yyyyMMdd)
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * 종료일자 파라미터 주입
     *
     * @param endDate 종료일자(yyyyMMdd)
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Map<String, Object> params = buildParameterMap();
            // 기존 사원 정보 갱신
            int updateCount = session.update("insaStgToLoc.updateEmployee", params);
            // ESNTL_ID 시퀀스 초기화
            session.update("insaStgToLoc.initEmployeeSeq");
            // 신규 사원 정보 추가
            int insertCount = session.insert("insaStgToLoc.insertEmployeeIncremental", params);
            session.commit();

            // 처리 건수를 스텝 컨트리뷰션에 반영
            int total = updateCount + insertCount;
            // StepExecution을 직접 사용하여 읽기 건수를 설정
            contribution.getStepExecution().setReadCount(total);
            contribution.incrementWriteCount(total);
        }
        return RepeatStatus.FINISHED;
    }

    /**
     * 잡 파라미터(startDate, endDate)를 MyBatis에 전달할 Map으로 구성한다.
     *
     * @return MyBatis 파라미터 맵
     */
    private Map<String, Object> buildParameterMap() {
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.hasText(startDate)) {
            params.put("startDate", startDate);
        }
        if (StringUtils.hasText(endDate)) {
            params.put("endDate", endDate);
        }
        return params;
    }
}
