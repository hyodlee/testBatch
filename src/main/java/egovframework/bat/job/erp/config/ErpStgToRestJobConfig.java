package egovframework.bat.job.erp.config;

import egovframework.bat.job.erp.tasklet.SendErpDataTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * STG DB에 적재된 ERP 데이터를 외부 REST API로 전송하는 잡 구성.
 */
@Configuration
public class ErpStgToRestJobConfig {

    /**
     * STG DB 데이터를 외부 시스템으로 전송하는 스텝을 정의한다.
     */
    @Bean
    public Step sendErpDataStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            SendErpDataTasklet sendErpDataTasklet) {
        return new StepBuilder("sendErpDataStep").repository(jobRepository)
                .tasklet(sendErpDataTasklet)
                .transactionManager(transactionManager)
                .build();
    }

    /**
     * 단일 스텝으로 구성된 STG→REST 데이터 전송 잡을 정의한다.
     */
    @Bean
    public Job erpStgToRestJob(JobRepository jobRepository,
            Step sendErpDataStep) {
        return new JobBuilder("erpStgToRestJob").repository(jobRepository)
                .start(sendErpDataStep)
                .build();
    }
}
