package egovframework.bat.insa.config;

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

import egovframework.bat.insa.domain.EmployeeInfo;
import egovframework.bat.insa.domain.Orgnztinfo;
import egovframework.bat.insa.listener.StepCountLogger;
import egovframework.bat.insa.tasklet.TruncateStgTablesTasklet;

/**
 * Remote1 시스템의 조직/사원 정보를 STG로 이관하는 잡 구성.
 */
@Configuration
public class InsaRemote1ToStgJobConfig {

    /**
     * STG 테이블을 비우는 Tasklet 빈.
     *
     * @param sqlSessionFactory STG용 SqlSessionFactory
     * @return TruncateStgTablesTasklet 인스턴스
     */
    @Bean
    public TruncateStgTablesTasklet truncateStgTablesTasklet(
            @Qualifier("sqlSessionFactory-stg") SqlSessionFactory sqlSessionFactory) {
        TruncateStgTablesTasklet tasklet = new TruncateStgTablesTasklet();
        tasklet.setSqlSessionFactory(sqlSessionFactory);
        return tasklet;
    }

    /**
     * remote1 DB에서 조직 정보를 읽어오는 리더.
     */
    @Bean
    @StepScope
    public EgovMyBatisPagingItemReader<Orgnztinfo> remote1ToStgOrgnztReader(
            @Qualifier("insa-sqlSessionFactory-remote1") SqlSessionFactory sqlSessionFactory) {
        EgovMyBatisPagingItemReader<Orgnztinfo> reader = new EgovMyBatisPagingItemReader<>();
        reader.setSqlSessionFactory(sqlSessionFactory);
        reader.setQueryId("insaRemToStg.selectOrgnztList");
        reader.setPageSize(100);
        return reader;
    }

    /**
     * STG DB에 조직 정보를 적재하는 라이터.
     */
    @Bean
    @StepScope
    public EgovMyBatisBatchItemWriter<Orgnztinfo> remote1ToStgOrgnztWriter(
            @Qualifier("sqlSessionFactory-stg") SqlSessionFactory sqlSessionFactory) {
        EgovMyBatisBatchItemWriter<Orgnztinfo> writer = new EgovMyBatisBatchItemWriter<>();
        writer.setSqlSessionFactory(sqlSessionFactory);
        writer.setStatementId("insaRemToStg.insertOrganization");
        return writer;
    }

    /**
     * remote1 DB에서 사원 정보를 읽어오는 리더.
     */
    @Bean
    @StepScope
    public EgovMyBatisPagingItemReader<EmployeeInfo> remote1ToStgEmpReader(
            @Qualifier("insa-sqlSessionFactory-remote1") SqlSessionFactory sqlSessionFactory) {
        EgovMyBatisPagingItemReader<EmployeeInfo> reader = new EgovMyBatisPagingItemReader<>();
        reader.setSqlSessionFactory(sqlSessionFactory);
        reader.setQueryId("insaRemToStg.selectEmployeeList");
        reader.setPageSize(100);
        return reader;
    }

    /**
     * STG DB에 사원 정보를 적재하는 라이터.
     */
    @Bean
    @StepScope
    public EgovMyBatisBatchItemWriter<EmployeeInfo> remote1ToStgEmpWriter(
            @Qualifier("sqlSessionFactory-stg") SqlSessionFactory sqlSessionFactory) {
        EgovMyBatisBatchItemWriter<EmployeeInfo> writer = new EgovMyBatisBatchItemWriter<>();
        writer.setSqlSessionFactory(sqlSessionFactory);
        writer.setStatementId("insaRemToStg.insertEmployee");
        return writer;
    }

    /**
     * STG 테이블을 비우는 스텝 정의.
     */
    @Bean
    public Step truncateStgTablesStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            TruncateStgTablesTasklet truncateStgTablesTasklet,
            StepCountLogger stepCountLogger) {
        return new StepBuilder("truncateStgTablesStep").repository(jobRepository)
                .tasklet(truncateStgTablesTasklet)
                .transactionManager(transactionManager)
                .listener(stepCountLogger)
                .build();
    }

    /**
     * 조직 정보를 이관하는 스텝 정의.
     */
    @Bean
    public Step remote1ToStgOrgnztStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Orgnztinfo> remote1ToStgOrgnztReader,
            ItemWriter<Orgnztinfo> remote1ToStgOrgnztWriter,
            StepCountLogger stepCountLogger) {
        return new StepBuilder("remote1ToStgOrgnztStep").repository(jobRepository)
                .<Orgnztinfo, Orgnztinfo>chunk(500)
                .reader(remote1ToStgOrgnztReader)
                .writer(remote1ToStgOrgnztWriter)
                .listener(stepCountLogger)
                .transactionManager(transactionManager)
                .build();
    }

    /**
     * 사원 정보를 이관하는 스텝 정의.
     */
    @Bean
    public Step remote1ToStgStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<EmployeeInfo> remote1ToStgEmpReader,
            ItemWriter<EmployeeInfo> remote1ToStgEmpWriter,
            StepCountLogger stepCountLogger) {
        return new StepBuilder("remote1ToStgStep").repository(jobRepository)
                .<EmployeeInfo, EmployeeInfo>chunk(500)
                .reader(remote1ToStgEmpReader)
                .writer(remote1ToStgEmpWriter)
                .listener(stepCountLogger)
                .transactionManager(transactionManager)
                .build();
    }

    /**
     * Remote1 데이터를 STG로 이관하는 잡 정의.
     */
    @Bean
    public Job insaRemote1ToStgJob(JobRepository jobRepository,
            Step truncateStgTablesStep,
            Step remote1ToStgOrgnztStep,
            Step remote1ToStgStep) {
        return new JobBuilder("insaRemote1ToStgJob").repository(jobRepository)
                .start(truncateStgTablesStep)
                .next(remote1ToStgOrgnztStep)
                .next(remote1ToStgStep)
                .build();
    }
}

