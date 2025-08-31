package egovframework.bat.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Service;

/**
 * 배치 잡 관리 기능을 제공하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class BatchManagementService {

    /** 배치 메타데이터 조회를 위한 매퍼 */
    private final BatchManagementMapper batchManagementMapper;

    /** 잡 재실행 및 중지를 위한 JobOperator */
    private final JobOperator jobOperator;

    /**
     * 등록된 배치 잡 이름 목록을 반환한다.
     *
     * @return 잡 이름 목록
     */
    public List<String> getJobNames() {
        return batchManagementMapper.selectJobNames();
    }

    /**
     * 특정 잡의 실행 이력을 조회한다.
     *
     * @param jobName 잡 이름
     * @return 실행 이력 목록
     */
    public List<JobExecution> getJobExecutions(String jobName) {
        return batchManagementMapper.selectJobExecutions(jobName);
    }

    /**
     * 특정 잡 실행에 대한 에러 로그를 조회한다.
     *
     * @param jobExecutionId 잡 실행 ID
     * @return 에러 로그 목록
     */
    public List<String> getErrorLogs(Long jobExecutionId) {
        return batchManagementMapper.selectErrorLogs(jobExecutionId);
    }

    /**
     * 실패한 잡 실행을 재시도한다.
     *
     * @param jobExecutionId 재실행할 잡 실행 ID
     * @throws Exception JobOperator에서 발생한 예외
     */
    public void restart(Long jobExecutionId) throws Exception {
        jobOperator.restart(jobExecutionId);
    }

    /**
     * 실행 중인 잡을 중지한다.
     *
     * @param jobExecutionId 중지할 잡 실행 ID
     * @throws Exception JobOperator에서 발생한 예외
     */
    public void stop(Long jobExecutionId) throws Exception {
        jobOperator.stop(jobExecutionId);
    }
}

