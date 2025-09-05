package egovframework.bat.job.example.api;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import egovframework.bat.job.common.api.JobRunController;

/**
 * 마이바티스 예제 배치 잡 실행 컨트롤러.
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class ExampleJobController {

    // 공통 잡 실행 컨트롤러에 위임
    private final JobRunController jobRunController;

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
        return jobRunController.execute("mybatisToMybatisSampleJob", jobParameters);
    }
}
