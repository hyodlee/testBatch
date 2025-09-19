package egovframework.bat.job.risk.api;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.bat.job.common.api.JobRunController;
import lombok.RequiredArgsConstructor;

/**
 * 리스크 원격 시스템 데이터를 STG로 적재하는 잡 실행 컨트롤러.
 */
@RestController
@RequestMapping("/api/batch/risk")
@RequiredArgsConstructor
public class RiskRemote1ToStgJobController {

    /** 공통 잡 실행 컨트롤러 */
    private final JobRunController jobRunController;

    /**
     * 리스크 원격 데이터를 STG로 이관한다.
     * @return 배치 잡 실행 결과
     */
    @PostMapping("/remote1-to-stg")
    public ResponseEntity<BatchStatus> runRiskRemote1ToStgJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        return jobRunController.execute("riskRemote1ToStgJob", jobParameters);
    }
}
