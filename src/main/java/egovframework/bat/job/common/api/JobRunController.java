package egovframework.bat.job.common.api;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import egovframework.bat.management.JobProgressService;
import egovframework.bat.service.JobLockService;

/**
 * 공통 배치 잡 실행 컨트롤러.
 * 지정된 잡 이름을 받아 실행한다.
 */
@RestController
@RequiredArgsConstructor
public class JobRunController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunController.class);

    // JobRegistry를 통해 잡을 조회
    private final JobRegistry jobRegistry;

    // 스프링 배치 잡 실행기
    private final JobLauncher jobLauncher;

    // 중복 실행 방지를 위한 락 서비스
    private final JobLockService jobLockService;

    // 진행 상황 전송 서비스
    private final JobProgressService jobProgressService;

    /**
     * 잡 이름을 받아 실행한다.
     *
     * @param jobName 실행할 잡의 빈 이름
     * @return 실행 결과 상태
     */
    @PostMapping("/api/batch/run")
    public ResponseEntity<BatchStatus> run(@RequestParam("jobName") String jobName) {
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();
        return execute(jobName, jobParameters);
    }

    /**
     * 주어진 파라미터로 잡을 실행한다.
     * 다른 컨트롤러에서 재사용할 수 있다.
     *
     * @param jobName       실행할 잡 이름
     * @param jobParameters 잡 파라미터
     * @return 실행 결과 상태
     */
    public ResponseEntity<BatchStatus> execute(String jobName, JobParameters jobParameters) {
        try {
            Job job = jobRegistry.getJob(jobName);
            if (!jobLockService.tryLock(jobName)) {
                LOGGER.warn("{} 작업이 이미 실행 중", jobName);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(BatchStatus.FAILED);
            }
            jobProgressService.send(jobName, "STARTED");
            try {
                JobExecution execution = jobLauncher.run(job, jobParameters);
                jobProgressService.send(jobName, execution.getStatus().toString());
                return ResponseEntity.ok(execution.getStatus());
            } finally {
                jobLockService.unlock(jobName);
            }
        } catch (Exception e) {
            LOGGER.error("배치 실행 실패", e);
            jobProgressService.send(jobName, "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BatchStatus.FAILED);
        }
    }
}
