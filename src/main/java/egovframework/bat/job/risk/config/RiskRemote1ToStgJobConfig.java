package egovframework.bat.job.risk.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.egovframe.rte.bat.core.item.database.EgovMyBatisBatchItemWriter;
import org.egovframe.rte.bat.core.item.database.EgovMyBatisPagingItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
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
import egovframework.bat.job.risk.domain.RiskIncident;
import egovframework.bat.job.risk.tasklet.TruncateRiskStgTablesTasklet;

/**
 * 리스크 원격 시스템 데이터를 STG로 이관하는 잡 설정.
 */
@Configuration
public class RiskRemote1ToStgJobConfig {

    /**
     * 리스크 STG 테이블을 비우는 Tasklet 빈.
     */
    @Bean
    public TruncateRiskStgTablesTasklet riskTruncateStgTablesTasklet(
            @Qualifier("sqlSessionFactory-stg") SqlSessionFactory sqlSessionFactory) {
        TruncateRiskStgTablesTasklet tasklet = new TruncateRiskStgTablesTasklet();
        tasklet.setSqlSessionFactory(sqlSessionFactory);
        return tasklet;
    }

    /**
     * 원격 DB에서 리스크 카테고리를 읽어오는 리더.
     */
    @Bean
    @StepScope
    public EgovMyBatisPagingItemReader<RiskCategory> riskRemote1ToStgCategoryReader(
            @Qualifier("risk-sqlSessionFactory-remote1") SqlSessionFactory sqlSessionFactory) {
        EgovMyBatisPagingItemReader<RiskCategory> reader = new EgovMyBatisPagingItemReader<>();
        reader.setSqlSessionFactory(sqlSessionFactory);
        reader.setQueryId("riskRemToStg.selectCategoryList");
        reader.setPageSize(100);
        return reader;
    }

    /**
     * STG DB에 리스크 카테고리를 기록하는 라이터.
     */
    @Bean
    @StepScope
    public EgovMyBatisBatchItemWriter<RiskCategory> riskRemote1ToStgCategoryWriter(
            @Qualifier("sqlSessionFactory-stg") SqlSessionFactory sqlSessionFactory) {
        EgovMyBatisBatchItemWriter<RiskCategory> writer = new EgovMyBatisBatchItemWriter<>();
        writer.setSqlSessionFactory(sqlSessionFactory);
        writer.setStatementId("riskRemToStg.insertCategory");
        return writer;
    }

    /**
     * 원격 DB에서 리스크 사건을 읽어오는 리더.
     */
    @Bean
    @StepScope
    public EgovMyBatisPagingItemReader<RiskIncident> riskRemote1ToStgIncidentReader(
            @Qualifier("risk-sqlSessionFactory-remote1") SqlSessionFactory sqlSessionFactory) {
        EgovMyBatisPagingItemReader<RiskIncident> reader = new EgovMyBatisPagingItemReader<>();
        reader.setSqlSessionFactory(sqlSessionFactory);
        reader.setQueryId("riskRemToStg.selectIncidentList");
        reader.setPageSize(100);
        return reader;
    }

    /**
     * STG DB에 리스크 사건을 기록하는 라이터.
     */
    @Bean
    @StepScope
    public EgovMyBatisBatchItemWriter<RiskIncident> riskRemote1ToStgIncidentWriter(
            @Qualifier("sqlSessionFactory-stg") SqlSessionFactory sqlSessionFactory) {
        EgovMyBatisBatchItemWriter<RiskIncident> writer = new EgovMyBatisBatchItemWriter<>();
        writer.setSqlSessionFactory(sqlSessionFactory);
        writer.setStatementId("riskRemToStg.insertIncident");
        return writer;
    }

    /**
     * STG 테이블 초기화 스텝.
     */
    @Bean
    public Step riskTruncateStgTablesStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            TruncateRiskStgTablesTasklet riskTruncateStgTablesTasklet,
            StepCountLogger stepCountLogger) {
        return new StepBuilder("riskTruncateStgTablesStep").repository(jobRepository)
                .tasklet(riskTruncateStgTablesTasklet)
                .transactionManager(transactionManager)
                .listener(stepCountLogger)
                .build();
    }

    /**
     * 리스크 카테고리를 이관하는 스텝.
     */
    @Bean
    public Step riskRemote1ToStgCategoryStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<RiskCategory> riskRemote1ToStgCategoryReader,
            ItemWriter<RiskCategory> riskRemote1ToStgCategoryWriter,
            StepCountLogger stepCountLogger) {
        return new StepBuilder("riskRemote1ToStgCategoryStep").repository(jobRepository)
                .<RiskCategory, RiskCategory>chunk(500)
                .reader(riskRemote1ToStgCategoryReader)
                .writer(riskRemote1ToStgCategoryWriter)
                .listener(stepCountLogger)
                .transactionManager(transactionManager)
                .build();
    }

    /**
     * 리스크 사건을 이관하는 스텝.
     */
    @Bean
    public Step riskRemote1ToStgIncidentStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<RiskIncident> riskRemote1ToStgIncidentReader,
            ItemWriter<RiskIncident> riskRemote1ToStgIncidentWriter,
            StepCountLogger stepCountLogger) {
        return new StepBuilder("riskRemote1ToStgIncidentStep").repository(jobRepository)
                .<RiskIncident, RiskIncident>chunk(500)
                .reader(riskRemote1ToStgIncidentReader)
                .writer(riskRemote1ToStgIncidentWriter)
                .listener(stepCountLogger)
                .transactionManager(transactionManager)
                .build();
    }

    /**
     * 리스크 원격 데이터를 STG로 옮기는 잡 정의.
     */
    @Bean
    public Job riskRemote1ToStgJob(JobRepository jobRepository,
            Step riskTruncateStgTablesStep,
            Step riskRemote1ToStgCategoryStep,
            Step riskRemote1ToStgIncidentStep) {
        return new JobBuilder("riskRemote1ToStgJob").repository(jobRepository)
                .start(riskTruncateStgTablesStep)
                .next(riskRemote1ToStgCategoryStep)
                .next(riskRemote1ToStgIncidentStep)
                .build();
    }
}
