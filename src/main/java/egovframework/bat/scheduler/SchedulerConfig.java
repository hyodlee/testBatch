package egovframework.bat.scheduler;

import org.quartz.JobDetail;
import org.quartz.JobListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * DB의 크론 정보를 이용해 스케줄을 설정하는 구성 클래스.
 */
@Configuration
public class SchedulerConfig {

    /**
     * SchedulerFactoryBean을 생성하여 글로벌 리스너와 기본 JobDetail을 등록한다.
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(
            @Qualifier("insaStgToLocalJobDetail") JobDetail insaStgToLocalJobDetail,
            @Qualifier("erpStgToLocalJobDetail") JobDetail erpStgToLocalJobDetail,
            @Qualifier("jobChainingJobListener") JobListener jobChainingJobListener) {
        SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();
        factoryBean.setJobDetails(insaStgToLocalJobDetail, erpStgToLocalJobDetail);
        factoryBean.setGlobalJobListeners(jobChainingJobListener);
        return factoryBean;
    }
}
