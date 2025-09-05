package egovframework.bat.job.example.api;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import egovframework.bat.management.JobProgressService;
import egovframework.bat.service.JobLockService;

/**
 * 마이바티스 예제 배치 잡을 REST API로 실행하기 위한 컨트롤러.
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class ExampleJobController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleJobController.class);

    // 스프링 배치 잡 실행기
    private final JobLauncher jobLauncher;

    // @Qualifier로 주입된 배치 잡
    @Qualifier("mybatisToMybatisSampleJob")
    private final Job mybatisToMybatisSampleJob;

    // 중복 실행 방지를 위한 락 서비스
    private final JobLockService jobLockService;

    // 진행 상황 전송 서비스
    private final JobProgressService jobProgressService;

    /**
     * 마이바티스 배치 잡을 실행한다.
     *
     * @param userId 사용자를 식별하기 위한 값 (선택)
     * @return 배치 잡 실행 결과 상태
     */
    @PostMapping("/mybatis")
    public ResponseEntity<BatchStatus> runMybatisJob(
        @RequestParam(value = "userId", required = false) String userId) {
        JobParametersBuilder builder = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis());

        if (userId != null) {
            builder.addString("userId", userId);
        }

        JobParameters jobParameters = builder.toJobParameters();
        // 잡 이름은 예외 처리에서도 사용되므로 try 블록 밖에서 정의
        String jobName = mybatisToMybatisSampleJob.getName();

        try {
            // @Qualifier로 주입된 잡 실행
            if (!jobLockService.tryLock(jobName)) {
                LOGGER.warn("{} 작업이 이미 실행 중", jobName);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(BatchStatus.FAILED);
            }
            jobProgressService.send(jobName, "STARTED");
            try {
                JobExecution execution = jobLauncher.run(mybatisToMybatisSampleJob, jobParameters);
                jobProgressService.send(jobName, execution.getStatus().toString());
                return ResponseEntity.ok(execution.getStatus());
            } finally {
                jobLockService.unlock(jobName);
            }
        } catch (Exception e) {
            LOGGER.error("마이바티스 배치 실행 실패", e);
            jobProgressService.send(jobName, "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BatchStatus.FAILED);
        }
    }
}

