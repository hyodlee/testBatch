package egovframework.bat.example.api;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
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
public class MybatisJobController {

    // 스프링 배치 잡 실행기
    private final JobLauncher jobLauncher;

    // MyBatis 데이터를 MyBatis 데이터베이스로 옮기는 배치 잡
    private final Job mybatisToMybatisJob;

    /**
     * 마이바티스 배치 잡을 실행한다.
     *
     * @param userId 사용자를 식별하기 위한 값 (선택)
     * @return 배치 잡 실행 결과 상태
     * @throws Exception 배치 실행 중 발생한 예외
     */
    @PostMapping("/mybatis")
    public BatchStatus runMybatisJob(@RequestParam(value = "userId", required = false) String userId) throws Exception {
        JobParametersBuilder builder = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis());

        if (userId != null) {
            builder.addString("userId", userId);
        }

        JobParameters jobParameters = builder.toJobParameters();
        JobExecution execution = jobLauncher.run(mybatisToMybatisJob, jobParameters);
        return execution.getStatus();
    }
}

