package egovframework.bat.config;

import javax.sql.DataSource;

import org.egovframe.rte.bat.core.launch.support.EgovBatchRunner;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * XML 기반 배치 런처 설정(context-batch-job-launcher.xml)을
 * 자바 구성으로 옮긴 클래스이다.
 */
@Configuration
@EnableBatchProcessing
@ImportResource("classpath:/egovframework/batch/context-batch-mapper.xml")
public class BatchJobLauncherConfig {

    /**
     * 배치 실행을 돕는 러너 빈 정의
     */
    @Bean
    public EgovBatchRunner eGovBatchRunner(JobOperator jobOperator,
            JobExplorer jobExplorer, JobRepository jobRepository) {
        return new EgovBatchRunner(jobOperator, jobExplorer, jobRepository);
    }

    /**
     * 단순 잡 런처 설정
     */
    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        return launcher;
    }

    /**
     * 잡 레지스트리 후처리기 등록
     */
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor processor = new JobRegistryBeanPostProcessor();
        processor.setJobRegistry(jobRegistry);
        return processor;
    }

    /**
     * 잡 레포지토리 설정
     */
    @Bean
    public JobRepository jobRepository(DataSource dataSource,
            PlatformTransactionManager transactionManager,
            LobHandler lobHandler) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setLobHandler(lobHandler);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    /**
     * 잡 오퍼레이터 설정
     */
    @Bean
    public JobOperator jobOperator(JobLauncher jobLauncher, JobExplorer jobExplorer,
            JobRepository jobRepository, JobRegistry jobRegistry) {
        SimpleJobOperator operator = new SimpleJobOperator();
        operator.setJobLauncher(jobLauncher);
        operator.setJobExplorer(jobExplorer);
        operator.setJobRepository(jobRepository);
        operator.setJobRegistry(jobRegistry);
        return operator;
    }

    /**
     * 잡 탐색기 설정
     */
    @Bean
    public JobExplorer jobExplorer(DataSource dataSource) throws Exception {
        JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
        factory.setDataSource(dataSource);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    /**
     * 잡 레지스트리 빈 정의
     */
    @Bean
    public JobRegistry jobRegistry() {
        return new MapJobRegistry();
    }

    /**
     * 지연 커넥션 프록시를 사용하는 JdbcTemplate
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        LazyConnectionDataSourceProxy proxy = new LazyConnectionDataSourceProxy(dataSource);
        return new JdbcTemplate(proxy);
    }
}

