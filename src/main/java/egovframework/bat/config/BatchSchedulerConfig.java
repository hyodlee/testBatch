package egovframework.bat.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.listeners.JobChainingJobListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import egovframework.bat.management.JobProgressService;
import egovframework.bat.scheduler.EgovQuartzJobLauncher;
import egovframework.bat.service.JobLockService;

/**
 * XML 기반 스케줄러 설정을 자바 기반으로 전환한 구성 클래스이다.
 * 잡 이름과 크론 표현식을 프로퍼티에서 읽어 동적으로 스케줄러에 등록한다.
 */
@Configuration
@ConfigurationProperties(prefix = "scheduler")
public class BatchSchedulerConfig {

    /** 프로퍼티에서 주입받은 잡 이름과 크론 표현식 */
    private Map<String, String> jobs = new HashMap<>();

    public Map<String, String> getJobs() {
        return jobs;
    }

    public void setJobs(Map<String, String> jobs) {
        this.jobs = jobs;
    }

    /**
     * 공통 JobDetail 생성 메서드
     */
    private JobDetail createJobDetail(Job job, JobLauncher jobLauncher,
            JobLockService jobLockService, JobProgressService jobProgressService,
            boolean durability, Map<String, Object> extraData) throws Exception {
        JobDetailFactoryBean factory = new JobDetailFactoryBean();
        factory.setName(job.getName() + "Detail");
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
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    /**
     * 크론 트리거 생성 메서드
     */
    private Trigger cronTrigger(JobDetail jobDetail, String expression) throws Exception {
        CronTriggerFactoryBean factory = new CronTriggerFactoryBean();
        factory.setJobDetail(jobDetail);
        factory.setCronExpression(expression);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    /**
     * 잡 체이닝 리스너
     */
    @Bean
    public JobChainingJobListener jobChainingJobListener() {
        JobChainingJobListener listener = new JobChainingJobListener("jobChainingListener");
        listener.addJobChainLink(new JobKey("insaRemote1ToStgJobDetail", "quartz-batch"),
                new JobKey("insaStgToLocalJobDetail", "quartz-batch"));
        listener.addJobChainLink(new JobKey("erpRestToStgJobDetail", "quartz-batch"),
                new JobKey("erpStgToLocalJobDetail", "quartz-batch"));
        return listener;
    }

    /**
     * 스케줄러 팩토리 빈
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobRegistry jobRegistry,
            JobLauncher jobLauncher, JobLockService jobLockService,
            JobProgressService jobProgressService,
            JobChainingJobListener jobChainingJobListener) throws Exception {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        List<Trigger> triggers = new ArrayList<>();
        List<JobDetail> jobDetails = new ArrayList<>();

        for (Map.Entry<String, String> entry : jobs.entrySet()) {
            String jobName = entry.getKey();
            String cron = entry.getValue();
            Job job = jobRegistry.getJob(jobName);

            boolean durability = (cron == null || cron.isEmpty());
            JobDetail jobDetail = createJobDetail(job, jobLauncher, jobLockService,
                    jobProgressService, durability, extraData(jobName));
            jobDetails.add(jobDetail);

            if (!durability) {
                triggers.add(cronTrigger(jobDetail, cron));
            }
        }

        factory.setJobDetails(jobDetails.toArray(new JobDetail[0]));
        factory.setTriggers(triggers.toArray(new Trigger[0]));
        factory.setGlobalJobListeners(jobChainingJobListener);
        return factory;
    }

    /**
     * 잡별 추가 데이터 설정
     */
    private Map<String, Object> extraData(String jobName) {
        if ("insaStgToLocalJob".equals(jobName)) {
            Map<String, Object> extra = new HashMap<>();
            extra.put("sourceSystem", "remote1");
            return extra;
        }
        return null;
    }
}

