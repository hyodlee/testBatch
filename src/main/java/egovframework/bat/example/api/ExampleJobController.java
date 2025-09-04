package egovframework.bat.example.api;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.configuration.JobRegistry;
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

    // 등록된 배치 잡을 조회하기 위한 잡 레지스트리
    private final JobRegistry jobRegistry;

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

        try {
            // 잡 레지스트리에서 잡을 조회한 뒤 실행한다
            Job job = jobRegistry.getJob("mybatisToMybatisSampleJob");
            String jobName = job.getName();
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
            LOGGER.error("마이바티스 배치 실행 실패", e);
            jobProgressService.send("mybatisToMybatisSampleJob", "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BatchStatus.FAILED);
        }
    }
}

