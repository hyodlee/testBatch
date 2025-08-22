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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    // MyBatis 데이터를 MyBatis 데이터베이스로 옮기는 배치 잡
    private final Job mybatisToMybatisJob;

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
            JobExecution execution = jobLauncher.run(mybatisToMybatisJob, jobParameters);
            return ResponseEntity.ok(execution.getStatus());
        } catch (Exception e) {
            LOGGER.error("마이바티스 배치 실행 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BatchStatus.FAILED);
        }
    }
}

