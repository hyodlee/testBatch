package egovframework.bat.job.insa.config;

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

import egovframework.bat.job.insa.domain.Orgnztinfo;
import egovframework.bat.job.insa.listener.StepCountLogger;
import egovframework.bat.job.insa.tasklet.StgToLocalEmployeeTasklet;

/**
 * STG에 적재된 조직/사원 정보를 로컬 DB로 이관하는 잡 구성.
 */
@Configuration
public class InsaStgToLocalJobConfig {

    /**
     * STG DB에서 조직 정보를 읽어오는 리더.
     */
    @Bean
    @StepScope
    public EgovMyBatisPagingItemReader<Orgnztinfo> stgToLocalOrgnztReader(
            @Qualifier("sqlSessionFactory-stg") SqlSessionFactory sqlSessionFactory) {
        EgovMyBatisPagingItemReader<Orgnztinfo> reader = new EgovMyBatisPagingItemReader<>();
        reader.setSqlSessionFactory(sqlSessionFactory);
        reader.setQueryId("insaStgToLoc.selectOrgnztList");
        reader.setPageSize(100);
        return reader;
    }

    /**
     * 로컬 DB에 조직 정보를 적재하는 라이터.
     */
    @Bean
    @StepScope
    public EgovMyBatisBatchItemWriter<Orgnztinfo> stgToLocalOrgnztWriter(
            @Qualifier("sqlSessionFactory-local") SqlSessionFactory sqlSessionFactory) {
        EgovMyBatisBatchItemWriter<Orgnztinfo> writer = new EgovMyBatisBatchItemWriter<>();
        writer.setSqlSessionFactory(sqlSessionFactory);
        writer.setStatementId("insaStgToLoc.insertOrganization");
        return writer;
    }

    /**
     * 조직 정보를 이관하는 스텝 정의.
     */
    @Bean
    public Step insaStgToLocalOrgnztStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Orgnztinfo> stgToLocalOrgnztReader,
            ItemWriter<Orgnztinfo> stgToLocalOrgnztWriter,
            StepCountLogger stepCountLogger) {
        return new StepBuilder("insaStgToLocalOrgnztStep").repository(jobRepository)
                .<Orgnztinfo, Orgnztinfo>chunk(500)
                .reader(stgToLocalOrgnztReader)
                .writer(stgToLocalOrgnztWriter)
                .listener(stepCountLogger)
                .transactionManager(transactionManager)
                .build();
    }

    /**
     * 사원 정보를 동기화하는 Tasklet 빈.
     */
    @Bean
    public StgToLocalEmployeeTasklet stgToLocalEmployeeTasklet(
            @Qualifier("sqlSessionFactory-local") SqlSessionFactory sqlSessionFactory) {
        StgToLocalEmployeeTasklet tasklet = new StgToLocalEmployeeTasklet();
        tasklet.setSqlSessionFactory(sqlSessionFactory);
        return tasklet;
    }

    /**
     * 사원 정보를 동기화하는 스텝 정의.
     */
    @Bean
    public Step insaStgToLocalEmployeeStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            StgToLocalEmployeeTasklet stgToLocalEmployeeTasklet,
            StepCountLogger stepCountLogger) {
        return new StepBuilder("insaStgToLocalEmployeeStep").repository(jobRepository)
                .tasklet(stgToLocalEmployeeTasklet)
                .listener(stepCountLogger)
                .transactionManager(transactionManager)
                .build();
    }

    /**
     * STG 데이터를 로컬 DB로 이관하는 잡 정의.
     */
    @Bean
    public Job insaStgToLocalJob(JobRepository jobRepository,
            Step insaStgToLocalOrgnztStep,
            Step insaStgToLocalEmployeeStep) {
        return new JobBuilder("insaStgToLocalJob").repository(jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(insaStgToLocalOrgnztStep)
                .next(insaStgToLocalEmployeeStep)
                .build();
    }
}

