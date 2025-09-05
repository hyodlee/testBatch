package egovframework.bat.job.erp.config;

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

import egovframework.bat.job.erp.domain.VehicleInfo;
import egovframework.bat.job.erp.processor.VehicleInfoProcessor;
import egovframework.bat.insa.listener.StepCountLogger;

/**
 * STG DB에 적재된 ERP 차량 정보를 로컬 DB로 이관하는 잡 구성.
 */
@Configuration
public class ErpStgToLocalJobConfig {

    /**
     * STG DB에서 ERP 차량 정보를 읽어오는 리더.
     *
     * @param sqlSessionFactory STG용 SqlSessionFactory
     * @return 차량 정보 리더
     */
    @Bean
    @StepScope
    public EgovMyBatisPagingItemReader<VehicleInfo> erpStgToLocalVehicleReader(
            @Qualifier("sqlSessionFactory-stg") SqlSessionFactory sqlSessionFactory) {
        EgovMyBatisPagingItemReader<VehicleInfo> reader = new EgovMyBatisPagingItemReader<>();
        reader.setSqlSessionFactory(sqlSessionFactory);
        reader.setQueryId("Vehicle.selectVehicleList");
        reader.setPageSize(100);
        return reader;
    }

    /**
     * 로컬 DB에 ERP 차량 정보를 적재하는 라이터.
     *
     * @param sqlSessionFactory 로컬 DB용 SqlSessionFactory
     * @return 차량 정보 라이터
     */
    @Bean
    @StepScope
    public EgovMyBatisBatchItemWriter<VehicleInfo> erpStgToLocalVehicleWriter(
            @Qualifier("sqlSessionFactory-local") SqlSessionFactory sqlSessionFactory) {
        EgovMyBatisBatchItemWriter<VehicleInfo> writer = new EgovMyBatisBatchItemWriter<>();
        writer.setSqlSessionFactory(sqlSessionFactory);
        writer.setStatementId("Vehicle.insertVehicleLocal");
        return writer;
    }

    /**
     * ERP 차량 정보를 이관하는 스텝 정의.
     *
     * @param jobRepository JobRepository
     * @param transactionManager 트랜잭션 매니저
     * @param erpStgToLocalVehicleReader 차량 정보 리더
     * @param vehicleInfoProcessor 차량 정보 프로세서
     * @param erpStgToLocalVehicleWriter 차량 정보 라이터
     * @param stepCountLogger 처리 건수 로깅 리스너
     * @return 차량 이관 스텝
     */
    @Bean
    public Step erpStgToLocalVehicleStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<VehicleInfo> erpStgToLocalVehicleReader,
            VehicleInfoProcessor vehicleInfoProcessor,
            ItemWriter<VehicleInfo> erpStgToLocalVehicleWriter,
            StepCountLogger stepCountLogger) {
        return new StepBuilder("erpStgToLocalVehicleStep").repository(jobRepository)
                .<VehicleInfo, VehicleInfo>chunk(500)
                .reader(erpStgToLocalVehicleReader)
                .processor(vehicleInfoProcessor)
                .writer(erpStgToLocalVehicleWriter)
                .listener(stepCountLogger)
                .transactionManager(transactionManager)
                .build();
    }

    /**
     * 단일 스텝으로 구성된 ERP 차량 정보 이관 잡 정의.
     *
     * @param jobRepository JobRepository
     * @param erpStgToLocalVehicleStep 차량 이관 스텝
     * @return ERP 차량 이관 잡
     */
    @Bean
    public Job erpStgToLocalJob(JobRepository jobRepository,
            Step erpStgToLocalVehicleStep) {
        return new JobBuilder("erpStgToLocalJob").repository(jobRepository)
                .start(erpStgToLocalVehicleStep)
                .build();
    }
}

