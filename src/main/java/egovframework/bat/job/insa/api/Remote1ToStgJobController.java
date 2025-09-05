package egovframework.bat.job.insa.api;

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
 * Remote1 데이터를 중간 저장소로 옮기는 배치 잡 실행 컨트롤러.
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class Remote1ToStgJobController {

    // 공통 잡 실행 컨트롤러에 위임
    private final JobRunController jobRunController;

    /**
     * Remote1 데이터를 STG로 적재한다.
     *
     * @param sourceSystem 데이터를 제공하는 시스템 이름 (선택)
     * @return 배치 잡 실행 결과 상태
     */
    @PostMapping("/remote1-to-stg")
    public ResponseEntity<BatchStatus> runRemote1ToStgJob(
        @RequestParam(value = "sourceSystem", required = false) String sourceSystem) {
        JobParametersBuilder builder = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis());
        if (sourceSystem != null) {
            builder.addString("sourceSystem", sourceSystem);
        }
        JobParameters jobParameters = builder.toJobParameters();
        return jobRunController.execute("insaRemote1ToStgJob", jobParameters);
    }
}
