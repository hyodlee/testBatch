package egovframework.bat.job.insa.api;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import egovframework.bat.job.common.api.JobRunController;
import lombok.RequiredArgsConstructor;

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
     * @param startDate 시작날짜 (선택)
     * @param endDate 종료날짜 (선택)
     * @return 배치 잡 실행 결과 상태
     */
    @PostMapping("/remote1-to-stg")
    public ResponseEntity<BatchStatus> runRemote1ToStgJob(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        JobParametersBuilder builder = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis());  //add를 여러개 연결해서 추가할수 있음. ExampleJobController참고

        if (startDate != null && !startDate.isBlank()) {
            builder.addString("startDate", startDate.trim());
        }
        if (endDate != null && !endDate.isBlank()) {
            builder.addString("endDate", endDate.trim());
        }

        JobParameters jobParameters = builder.toJobParameters();
        return jobRunController.execute("insaRemote1ToStgJob", jobParameters);
    }
}
