package egovframework.bat.config;

import java.util.HashMap;
import java.util.Map;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.listeners.JobChainingJobListener;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import egovframework.bat.management.JobProgressService;
import egovframework.bat.scheduler.EgovQuartzJobLauncher;
import egovframework.bat.service.JobLockService;

/**
 * XML 기반 스케줄러 설정(context-scheduler-job.xml, context-batch-scheduler.xml)을
 * 자바 구성으로 전환한 클래스이다.
 */
@Configuration
public class BatchSchedulerConfig {

    // 공통 JobDetail 생성 메서드
    private JobDetailFactoryBean createJobDetail(String jobName, JobRegistry jobRegistry,
            JobLauncher jobLauncher, JobLockService jobLockService,
            JobProgressService jobProgressService, boolean durability, Map<String, Object> extraData) {
        JobDetailFactoryBean factory = new JobDetailFactoryBean();
        factory.setJobClass(EgovQuartzJobLauncher.class);
        factory.setGroup("quartz-batch");
        factory.setDurability(durability);
        Map<String, Object> map = new HashMap<>();
        map.put("jobName", jobName);
        map.put("jobLocator", jobRegistry);
        map.put("jobLauncher", jobLauncher);
        map.put("jobLockService", jobLockService);
        map.put("jobProgressService", jobProgressService);
        if (extraData != null) {
            map.putAll(extraData);
        }
        factory.setJobDataAsMap(map);
        return factory;
    }

    /** 샘플 잡 */
    @Bean
    public JobDetailFactoryBean jobDetail(JobRegistry jobRegistry, JobLauncher jobLauncher,
            JobLockService jobLockService, JobProgressService jobProgressService) {
        return createJobDetail("mybatisToMybatisSampleJob", jobRegistry, jobLauncher,
                jobLockService, jobProgressService, false, null);
    }

    /** insaRemote1ToStg 잡 */
    @Bean
    public JobDetailFactoryBean insaRemote1ToStgJobDetail(JobRegistry jobRegistry, JobLauncher jobLauncher,
            JobLockService jobLockService, JobProgressService jobProgressService) {
        return createJobDetail("insaRemote1ToStgJob", jobRegistry, jobLauncher,
                jobLockService, jobProgressService, false, null);
    }

    /** insaStgToLocal 잡 (durable) */
    @Bean
    public JobDetailFactoryBean insaStgToLocalJobDetail(JobRegistry jobRegistry, JobLauncher jobLauncher,
            JobLockService jobLockService, JobProgressService jobProgressService) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("sourceSystem", "remote1");
        return createJobDetail("insaStgToLocalJob", jobRegistry, jobLauncher,
                jobLockService, jobProgressService, true, extra);
    }

    /** erpRestToStg 잡 */
    @Bean
    public JobDetailFactoryBean erpRestToStgJobDetail(JobRegistry jobRegistry, JobLauncher jobLauncher,
            JobLockService jobLockService, JobProgressService jobProgressService) {
        return createJobDetail("erpRestToStgJob", jobRegistry, jobLauncher,
                jobLockService, jobProgressService, false, null);
    }

    /** erpStgToLocal 잡 (durable) */
    @Bean
    public JobDetailFactoryBean erpStgToLocalJobDetail(JobRegistry jobRegistry, JobLauncher jobLauncher,
            JobLockService jobLockService, JobProgressService jobProgressService) {
        return createJobDetail("erpStgToLocalJob", jobRegistry, jobLauncher,
                jobLockService, jobProgressService, true, null);
    }

    // 크론 트리거 생성 메서드
    private CronTriggerFactoryBean cronTrigger(JobDetail jobDetail, String expression) {
        CronTriggerFactoryBean factory = new CronTriggerFactoryBean();
        factory.setJobDetail(jobDetail);
        factory.setCronExpression(expression);
        return factory;
    }

    /** insaRemote1ToStg 크론 트리거 */
    @Bean
    public CronTriggerFactoryBean insaRemote1ToStgCronTrigger(
            @Qualifier("insaRemote1ToStgJobDetail") JobDetail jobDetail) {
        return cronTrigger(jobDetail, "0 * * * * ?");
    }

    /** erpRestToStg 크론 트리거 */
    @Bean
    public CronTriggerFactoryBean erpRestToStgCronTrigger(
            @Qualifier("erpRestToStgJobDetail") JobDetail jobDetail) {
        return cronTrigger(jobDetail, "0 0/5 * * * ?");
    }

    /** 잡 체이닝 리스너 */
    @Bean
    public JobChainingJobListener jobChainingJobListener() {
        JobChainingJobListener listener = new JobChainingJobListener("jobChainingListener");
        listener.addJobChainLink(new JobKey("insaRemote1ToStgJobDetail", "quartz-batch"),
                new JobKey("insaStgToLocalJobDetail", "quartz-batch"));
        listener.addJobChainLink(new JobKey("erpRestToStgJobDetail", "quartz-batch"),
                new JobKey("erpStgToLocalJobDetail", "quartz-batch"));
        return listener;
    }

    /** 스케줄러 팩토리 빈 */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(
            @Qualifier("insaRemote1ToStgCronTrigger") Trigger insaRemote1ToStgCronTrigger,
            @Qualifier("erpRestToStgCronTrigger") Trigger erpRestToStgCronTrigger,
            @Qualifier("insaStgToLocalJobDetail") JobDetail insaStgToLocalJobDetail,
            @Qualifier("erpStgToLocalJobDetail") JobDetail erpStgToLocalJobDetail,
            JobChainingJobListener jobChainingJobListener) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setTriggers(insaRemote1ToStgCronTrigger, erpRestToStgCronTrigger);
        factory.setJobDetails(insaStgToLocalJobDetail, erpStgToLocalJobDetail);
        factory.setGlobalJobListeners(jobChainingJobListener);
        return factory;
    }
}

