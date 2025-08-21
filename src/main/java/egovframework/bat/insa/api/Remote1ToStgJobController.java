package egovframework.bat.insa.api;

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
 * Remote1 시스템의 데이터를 중간 저장소로 옮기는 배치 잡을 REST API로 실행하기 위한 컨트롤러.
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class Remote1ToStgJobController {

    // 스프링 배치 잡 실행기
    private final JobLauncher jobLauncher;

    // Remote1 데이터를 중간 저장소로 옮기는 배치 잡
    private final Job insaRemote1ToStgJob;

    /**
     * Remote1 데이터를 중간 저장소로 옮기는 배치 잡을 실행한다.
     *
     * @param sourceSystem 데이터를 제공하는 시스템 이름 (선택)
     * @return 배치 잡 실행 결과 상태
     * @throws Exception 배치 실행 중 발생한 예외
     */
    @PostMapping("/remote1-to-stg")
    public BatchStatus runRemote1ToStgJob(@RequestParam(value = "sourceSystem", required = false) String sourceSystem) throws Exception {
        JobParametersBuilder builder = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis());

        if (sourceSystem != null) {
            builder.addString("sourceSystem", sourceSystem);
        }

        JobParameters jobParameters = builder.toJobParameters();
        JobExecution execution = jobLauncher.run(insaRemote1ToStgJob, jobParameters);
        return execution.getStatus();
    }
}

