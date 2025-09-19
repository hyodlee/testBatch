package egovframework.bat.job.risk.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.egovframe.rte.bat.core.item.database.EgovMyBatisBatchItemWriter;
import org.egovframe.rte.bat.core.item.database.EgovMyBatisPagingItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import egovframework.bat.job.insa.listener.StepCountLogger;
import egovframework.bat.job.risk.domain.RiskCategory;
import egovframework.bat.job.risk.tasklet.RiskStgToLocalIncidentTasklet;

/**
 * STG에 적재된 리스크 데이터를 로컬 DB로 이관하는 잡 설정.
 */
@Configuration
public class RiskStgToLocalJobConfig {

    /**
     * STG DB에서 리스크 카테고리를 읽어오는 리더.
     */
    @Bean
    @StepScope
    public EgovMyBatisPagingItemReader<RiskCategory> riskStgToLocalCategoryReader(
            @Qualifier("sqlSessionFactory-stg") SqlSessionFactory sqlSessionFactory) {
        EgovMyBatisPagingItemReader<RiskCategory> reader = new EgovMyBatisPagingItemReader<>();
        reader.setSqlSessionFactory(sqlSessionFactory);
        reader.setQueryId("riskStgToLoc.selectCategoryList");
        reader.setPageSize(100);
        return reader;
    }

    /**
     * 로컬 DB에 리스크 카테고리를 적재하는 라이터.
     */
    @Bean
    @StepScope
    public EgovMyBatisBatchItemWriter<RiskCategory> riskStgToLocalCategoryWriter(
            @Qualifier("sqlSessionFactory-local") SqlSessionFactory sqlSessionFactory) {
        EgovMyBatisBatchItemWriter<RiskCategory> writer = new EgovMyBatisBatchItemWriter<>();
        writer.setSqlSessionFactory(sqlSessionFactory);
        writer.setStatementId("riskStgToLoc.insertCategory");
        return writer;
    }

    /**
     * 리스크 카테고리 이관 스텝 정의.
     */
    @Bean
    public Step riskStgToLocalCategoryStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<RiskCategory> riskStgToLocalCategoryReader,
            ItemWriter<RiskCategory> riskStgToLocalCategoryWriter,
            StepCountLogger stepCountLogger) {
        return new StepBuilder("riskStgToLocalCategoryStep").repository(jobRepository)
                .<RiskCategory, RiskCategory>chunk(500)
                .reader(riskStgToLocalCategoryReader)
                .writer(riskStgToLocalCategoryWriter)
                .listener(stepCountLogger)
                .transactionManager(transactionManager)
                .build();
    }

    /**
     * 리스크 사건 동기화 Tasklet 빈.
     */
    @Bean
    public RiskStgToLocalIncidentTasklet riskStgToLocalIncidentTasklet(
            @Qualifier("sqlSessionFactory-local") SqlSessionFactory sqlSessionFactory) {
        RiskStgToLocalIncidentTasklet tasklet = new RiskStgToLocalIncidentTasklet();
        tasklet.setSqlSessionFactory(sqlSessionFactory);
        return tasklet;
    }

    /**
     * 리스크 사건 이관 스텝 정의.
     */
    @Bean
    public Step riskStgToLocalIncidentStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            RiskStgToLocalIncidentTasklet riskStgToLocalIncidentTasklet,
            StepCountLogger stepCountLogger) {
        return new StepBuilder("riskStgToLocalIncidentStep").repository(jobRepository)
                .tasklet(riskStgToLocalIncidentTasklet)
                .listener(stepCountLogger)
                .transactionManager(transactionManager)
                .build();
    }

    /**
     * 리스크 STG 데이터를 로컬 DB로 옮기는 잡.
     */
    @Bean
    public Job riskStgToLocalJob(JobRepository jobRepository,
            Step riskStgToLocalCategoryStep,
            Step riskStgToLocalIncidentStep) {
        return new JobBuilder("riskStgToLocalJob").repository(jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(riskStgToLocalCategoryStep)
                .next(riskStgToLocalIncidentStep)
                .build();
    }
}
