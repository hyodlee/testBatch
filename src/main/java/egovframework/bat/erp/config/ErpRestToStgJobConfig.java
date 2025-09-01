package egovframework.bat.erp.config;

import egovframework.bat.erp.tasklet.FetchErpDataTasklet;
import egovframework.bat.erp.tasklet.TruncateErpVehicleTasklet;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * ERP 시스템 데이터를 STG 테이블에 적재하는 잡 구성.
 */
@Configuration
public class ErpRestToStgJobConfig {

    /**
     * STG 차량 테이블을 비우는 Tasklet 빈을 정의한다.
     *
     * @param sqlSessionFactory STG용 SqlSessionFactory
     * @return TruncateErpVehicleTasklet 인스턴스
     */
    @Bean
    public TruncateErpVehicleTasklet truncateErpVehicleTasklet(
            @Qualifier("sqlSessionFactory-stg") SqlSessionFactory sqlSessionFactory) {
        TruncateErpVehicleTasklet tasklet = new TruncateErpVehicleTasklet();
        tasklet.setSqlSessionFactory(sqlSessionFactory);
        return tasklet;
    }

    /**
     * STG 차량 테이블을 비우는 스텝을 정의한다.
     */
    @Bean
    public Step truncateErpVehicleStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            TruncateErpVehicleTasklet truncateErpVehicleTasklet) {
        return new StepBuilder("truncateErpVehicleStep").repository(jobRepository)
                .tasklet(truncateErpVehicleTasklet, transactionManager)
                .build();
    }

    /**
     * ERP 차량 데이터를 조회하여 적재하는 스텝을 정의한다.
     */
    @Bean
    public Step fetchErpDataStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FetchErpDataTasklet fetchErpDataTasklet) {
        return new StepBuilder("fetchErpDataStep").repository(jobRepository)
                .tasklet(fetchErpDataTasklet, transactionManager)
                .build();
    }

    /**
     * 트렁케이션 후 데이터를 적재하는 잡을 정의한다.
     */
    @Bean
    public Job erpRestToStgJob(JobRepository jobRepository,
            Step truncateErpVehicleStep,
            Step fetchErpDataStep) {
        return new JobBuilder("erpRestToStgJob").repository(jobRepository)
                .start(truncateErpVehicleStep)
                .next(fetchErpDataStep)
                .build();
    }
}

