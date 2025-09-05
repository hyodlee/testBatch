package egovframework.bat.job.erp.api;

import java.util.Map;

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
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * STG 데이터 외부 ERP 시스템으로 전송 배치를 수동 실행하기 위한 컨트롤러.
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class StgToRestJobController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StgToRestJobController.class);

    /** 잡 실행기 */
    private final JobLauncher jobLauncher;

    /** STG 데이터를 외부로 전송하는 잡 */
    @Qualifier("erpStgToRestJob")
    private final Job erpStgToRestJob;

    /** 여러 잡을 이름으로 주입받기 위한 맵 */
    private final Map<String, Job> jobs;

    /**
     * ERP STG 데이터를 외부 REST API로 전송하는 잡을 실행한다.
     *
     * @return 배치 실행 결과 상태
     */
    @PostMapping("/erp-stg-to-rest")
    public ResponseEntity<BatchStatus> runErpStgToRestJob() {
        LOGGER.info("ERP STG→REST 배치 실행 요청 수신");
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();
        try {
            Job job = jobs.getOrDefault("erpStgToRestJob", erpStgToRestJob);
            JobExecution execution = jobLauncher.run(job, jobParameters);
            LOGGER.info("ERP STG→REST 배치 실행 완료: {}", execution.getStatus());
            return ResponseEntity.ok(execution.getStatus());
        } catch (Exception e) {
            LOGGER.error("ERP STG→REST 배치 실행 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BatchStatus.FAILED);
        }
    }
}

