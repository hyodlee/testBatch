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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ERP REST API 데이터를 STG 테이블로 적재하는 배치 잡을 수동 실행하기 위한 컨트롤러.
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class RestToStgJobController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestToStgJobController.class);

    // 스프링 배치 잡 실행기
    private final JobLauncher jobLauncher;

    // ERP REST 데이터를 STG 테이블에 적재하는 배치 잡
    private final Job erpRestToStgJob;

    /**
     * ERP REST API 데이터를 STG 테이블로 적재하는 배치 잡을 실행한다.
     *
     * @return 배치 잡 실행 결과 상태
     * @throws Exception 배치 실행 중 발생한 예외
     */
    @PostMapping("/erp-rest-to-stg")
    public BatchStatus runErpRestToStgJob() throws Exception {
        LOGGER.info("ERP REST 배치 실행 요청 수신");
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

        try {
            JobExecution execution = jobLauncher.run(erpRestToStgJob, jobParameters);
            LOGGER.info("ERP REST 배치 실행 완료: {}", execution.getStatus());
            return execution.getStatus();
        } catch (Exception e) {
            LOGGER.error("ERP REST 배치 실행 실패", e);
            throw e;
        }
    }
}

