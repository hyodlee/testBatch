package egovframework.bat.job.erp.api;

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
 * ERP REST 데이터를 STG 테이블로 적재하는 배치 잡 실행 컨트롤러.
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class RestToStgJobController {

    // 공통 잡 실행 컨트롤러에 위임
    private final JobRunController jobRunController;

    /**
     * ERP REST 데이터를 STG로 적재한다.
     *
     * @return 배치 잡 실행 결과 상태
     */
    @PostMapping("/erp-rest-to-stg")
    public ResponseEntity<BatchStatus> runErpRestToStgJob() {
    	JobParametersBuilder builder = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis());	//add를 여러개 연결해서 추가할수 있음. ExampleJobController참고
        JobParameters jobParameters = builder.toJobParameters();
        return jobRunController.execute("erpRestToStgJob", jobParameters);
    }
}
