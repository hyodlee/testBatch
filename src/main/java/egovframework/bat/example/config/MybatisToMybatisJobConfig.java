package egovframework.bat.example.config;

import org.egovframe.rte.bat.core.item.database.EgovMyBatisBatchItemWriter;
import org.egovframe.rte.bat.core.item.database.EgovMyBatisPagingItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import egovframework.bat.example.domain.CustomerCredit;
import egovframework.bat.example.processor.CustomerCreditIncreaseProcessor;

import org.apache.ibatis.session.SqlSessionFactory;

/**
 * MyBatis에서 MyBatis로 데이터를 옮기는 샘플 잡을 자바 DSL로 구성한다.
 */
@Configuration
public class MybatisToMybatisJobConfig {

    /**
     * 리더 빈 정의. remote1 DB에서 데이터를 페이지 단위로 조회한다.
     */
    @Bean
    @StepScope
    public EgovMyBatisPagingItemReader<CustomerCredit> mybatisItemReader(
            @Qualifier("example-sqlSessionFactory-remote1") SqlSessionFactory sqlSessionFactory) {
        EgovMyBatisPagingItemReader<CustomerCredit> reader = new EgovMyBatisPagingItemReader<>();
        reader.setSqlSessionFactory(sqlSessionFactory);
        reader.setQueryId("Customer.getAllCustomerCredits");
        reader.setPageSize(100);
        return reader;
    }

    /**
     * 라이터 빈 정의. 스테이징 DB에 결과를 적재한다.
     */
    @Bean
    @StepScope
    public EgovMyBatisBatchItemWriter<CustomerCredit> mybatisItemWriter(
            @Qualifier("sqlSessionFactory-stg") SqlSessionFactory sqlSessionFactory) {
        EgovMyBatisBatchItemWriter<CustomerCredit> writer = new EgovMyBatisBatchItemWriter<>();
        writer.setSqlSessionFactory(sqlSessionFactory);
        writer.setStatementId("Customer.updateCredit");
        return writer;
    }

    /**
     * 프로세서 빈 정의. 고객의 크레딧을 증가시킨다.
     */
    @Bean
    public CustomerCreditIncreaseProcessor itemProcessor() {
        return new CustomerCreditIncreaseProcessor();
    }

    /**
     * 스텝 정의. 청크 단위로 리더, 프로세서, 라이터를 연결한다.
     */
    @Bean
    public Step mybatisToMybatisStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<CustomerCredit> mybatisItemReader,
            ItemProcessor<CustomerCredit, CustomerCredit> itemProcessor,
            ItemWriter<CustomerCredit> mybatisItemWriter) {
        return new StepBuilder("mybatisToMybatisStep").repository(jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(500)
                .reader(mybatisItemReader)
                .processor(itemProcessor)
                .writer(mybatisItemWriter)
                .transactionManager(transactionManager) // 트랜잭션 매니저 설정
                .build();
    }

    /**
     * 잡 정의. 단일 스텝을 실행한다.
     */
    @Bean
    public Job mybatisToMybatisSampleJob(JobRepository jobRepository, Step mybatisToMybatisStep) {
        return new JobBuilder("mybatisToMybatisSampleJob").repository(jobRepository)
                .start(mybatisToMybatisStep)
                .build();
    }
}

