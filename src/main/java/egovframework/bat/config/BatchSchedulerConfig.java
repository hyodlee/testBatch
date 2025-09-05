package egovframework.bat.config;

import java.util.HashMap;
import java.util.Map;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.listeners.JobChainingJobListener;
import org.springframework.batch.core.Job;
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

    /** 공통 JobDetail 생성 메서드 */
    private JobDetailFactoryBean createJobDetail(Job job,
            JobLauncher jobLauncher, JobLockService jobLockService,
            JobProgressService jobProgressService, boolean durability, Map<String, Object> extraData) {
        JobDetailFactoryBean factory = new JobDetailFactoryBean();
        factory.setJobClass(EgovQuartzJobLauncher.class);
        factory.setGroup("quartz-batch");
        factory.setDurability(durability);
        Map<String, Object> map = new HashMap<>();
        map.put("job", job); // 실행할 Job 인스턴스
        map.put("jobName", job.getName()); // 잡 이름
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
    public JobDetailFactoryBean jobDetail(@Qualifier("mybatisToMybatisSampleJob") Job job,
            JobLauncher jobLauncher, JobLockService jobLockService,
            JobProgressService jobProgressService) {
        return createJobDetail(job, jobLauncher, jobLockService,
                jobProgressService, false, null);
    }

    /** insaRemote1ToStg 잡 */
    @Bean
    public JobDetailFactoryBean insaRemote1ToStgJobDetail(
            @Qualifier("insaRemote1ToStgJob") Job job, JobLauncher jobLauncher,
            JobLockService jobLockService, JobProgressService jobProgressService) {
        return createJobDetail(job, jobLauncher, jobLockService,
                jobProgressService, false, null);
    }

    /** insaStgToLocal 잡 (durable) */
    @Bean
    public JobDetailFactoryBean insaStgToLocalJobDetail(
            @Qualifier("insaStgToLocalJob") Job job, JobLauncher jobLauncher,
            JobLockService jobLockService, JobProgressService jobProgressService) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("sourceSystem", "remote1");
        return createJobDetail(job, jobLauncher, jobLockService,
                jobProgressService, true, extra);
    }

    /** erpRestToStg 잡 */
    @Bean
    public JobDetailFactoryBean erpRestToStgJobDetail(
            @Qualifier("erpRestToStgJob") Job job, JobLauncher jobLauncher,
            JobLockService jobLockService, JobProgressService jobProgressService) {
        return createJobDetail(job, jobLauncher, jobLockService,
                jobProgressService, false, null);
    }

    /** erpStgToLocal 잡 (durable) */
    @Bean
    public JobDetailFactoryBean erpStgToLocalJobDetail(
            @Qualifier("erpStgToLocalJob") Job job, JobLauncher jobLauncher,
            JobLockService jobLockService, JobProgressService jobProgressService) {
        return createJobDetail(job, jobLauncher, jobLockService,
                jobProgressService, true, null);
    }

    /** erpStgToRest 잡 */
    @Bean
    public JobDetailFactoryBean erpStgToRestJobDetail(
            @Qualifier("erpStgToRestJob") Job job, JobLauncher jobLauncher,
            JobLockService jobLockService, JobProgressService jobProgressService) {
        return createJobDetail(job, jobLauncher, jobLockService,
                jobProgressService, false, null);
    }

    /** 크론 트리거 생성 메서드 */
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

    /** erpStgToRest 크론 트리거 */
    @Bean
    public CronTriggerFactoryBean erpStgToRestCronTrigger(
            @Qualifier("erpStgToRestJobDetail") JobDetail jobDetail) {
        // 매 정각마다 실행
        return cronTrigger(jobDetail, "0 0 * * * ?");
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
            @Qualifier("erpStgToRestCronTrigger") Trigger erpStgToRestCronTrigger,
            @Qualifier("insaStgToLocalJobDetail") JobDetail insaStgToLocalJobDetail,
            @Qualifier("erpStgToLocalJobDetail") JobDetail erpStgToLocalJobDetail,
            JobChainingJobListener jobChainingJobListener) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        //작동을 멈추고 싶은 작업이 있으면, 아래의 작업이름에 주석을 하면 됨
        factory.setTriggers(
        		insaRemote1ToStgCronTrigger
        		, erpRestToStgCronTrigger
        		, erpStgToRestCronTrigger
        		);
        factory.setJobDetails(
        		insaStgToLocalJobDetail
        		, erpStgToLocalJobDetail
        		);
        factory.setGlobalJobListeners(jobChainingJobListener);
        return factory;
    }
}

