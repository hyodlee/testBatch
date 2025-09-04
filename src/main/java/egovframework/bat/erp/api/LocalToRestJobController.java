package egovframework.bat.erp.api;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.bat.management.JobProgressService;
import egovframework.bat.service.JobLockService;

/**
 * 로컬 DB의 ERP 데이터를 외부 REST API로 전송하는 배치 잡을 수동 실행하기 위한 컨트롤러.
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class LocalToRestJobController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalToRestJobController.class);

    // 배치 잡 실행기
    private final JobLauncher jobLauncher;

    // 로컬→REST 전송 잡
    private final Job erpLocalToRestJob;

    // 중복 실행 방지 서비스
    private final JobLockService jobLockService;

    // 진행 상황 전송 서비스
    private final JobProgressService jobProgressService;

    /**
     * 로컬 ERP 데이터를 외부 REST API로 전송하는 배치 잡을 실행한다.
     *
     * @return 배치 잡 실행 결과 상태
     */
    @PostMapping("/erp-local-to-rest")
    public ResponseEntity<BatchStatus> runErpLocalToRestJob() {
        LOGGER.info("ERP 로컬→REST 배치 실행 요청 수신");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        try {
            String jobName = erpLocalToRestJob.getName();
            if (!jobLockService.tryLock(jobName)) {
                LOGGER.warn("{} 작업이 이미 실행 중", jobName);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(BatchStatus.FAILED);
            }
            jobProgressService.send(jobName, "STARTED");
            try {
                JobExecution execution = jobLauncher.run(erpLocalToRestJob, jobParameters);
                LOGGER.info("ERP 로컬→REST 배치 실행 완료: {}", execution.getStatus());
                jobProgressService.send(jobName, execution.getStatus().toString());
                return ResponseEntity.ok(execution.getStatus());
            } finally {
                jobLockService.unlock(jobName);
            }
        } catch (Exception e) {
            LOGGER.error("ERP 로컬→REST 배치 실행 실패", e);
            jobProgressService.send("erpLocalToRestJob", "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BatchStatus.FAILED);
        }
    }
}

