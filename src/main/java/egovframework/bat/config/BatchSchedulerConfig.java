package egovframework.bat.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.listeners.JobChainingJobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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

    /** 로그 기록용 로거 */
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchSchedulerConfig.class);

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
     *
     * <p>application.yml의 {@code scheduler.jobs} 값을 읽어 크론이 있는 잡은
     * {@code CronTrigger}로 등록하고, 크론이 비어 있는 잡은 {@code JobDetail}만 생성한 뒤
     * {@code JobChainingJobListener}로 체인 처리한다.</p>
     */
    @Bean
    @DependsOn("jobRegistryBeanPostProcessor") // 잡 등록이 완료된 후 스케줄러 생성
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

            Job job;
            try {
                job = jobRegistry.getJob(jobName);
            } catch (NoSuchJobException e) {
                // 등록되지 않은 잡은 경고 로그 후 건너뜀
                LOGGER.warn("등록되지 않은 잡 '{}'을(를) 건너뜁니다. 사유: {}", jobName, e.getMessage());
                continue;
            }

            boolean durability = (cron == null || cron.isEmpty());
            JobDetail jobDetail = createJobDetail(job, jobLauncher, jobLockService,
                    jobProgressService, durability, extraData(jobName));

            if (durability) {
                // 크론이 없으면 영속 JobDetail로만 등록
                jobDetails.add(jobDetail);
            } else {
                // 크론이 있으면 트리거만 등록
                triggers.add(cronTrigger(jobDetail, cron));
            }
        }

        // 영속 잡들만 스케줄러에 직접 등록
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

