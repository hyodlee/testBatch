package egovframework.bat.erp.config;

import egovframework.bat.erp.tasklet.SendErpDataTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 로컬 DB의 ERP 데이터를 외부 REST API로 전송하는 잡 구성.
 */
@Configuration
public class ErpLocalToRestJobConfig {

    /**
     * 로컬 데이터를 외부 ERP로 전송하는 스텝 정의.
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
     * 단일 스텝으로 구성된 로컬→ERP 전송 잡 정의.
     */
    @Bean
    public Job erpLocalToRestJob(JobRepository jobRepository,
                                 Step sendErpDataStep) {
        return new JobBuilder("erpLocalToRestJob").repository(jobRepository)
                .start(sendErpDataStep)
                .build();
    }
}

