package egovframework.bat.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.listeners.JobChainingJobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import egovframework.bat.repository.dto.SchedulerJobDto;
import egovframework.bat.scheduler.EgovQuartzJobLauncher;
import lombok.RequiredArgsConstructor;

/**
 * XML 기반 스케줄러 설정을 자바 기반으로 전환한 구성 클래스이다.
 * 잡 이름과 크론 표현식을 Quartz 테이블에서 읽어 동적으로 스케줄러에 등록한다.
 */

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SchedulerProps.class)
public class BatchSchedulerConfig {

    /** 로그 기록용 로거 */
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchSchedulerConfig.class);

    /** application.yml에서 읽어 온 scheduler 정보 */
    private final SchedulerProps schedulerProps;


    /**
     * 공통 JobDetail 생성 메서드
     */
    private JobDetail createJobDetail(Job job, boolean durability,
            Map<String, Object> extraData) throws Exception {
        JobDetailFactoryBean factory = new JobDetailFactoryBean();
        factory.setName(job.getName() + "Detail");
        factory.setJobClass(EgovQuartzJobLauncher.class);
        factory.setGroup("quartz-batch");
        factory.setDurability(durability);

        Map<String, Object> map = new HashMap<>();
        // 잡 이름만 JobDataMap에 저장하여 직렬화 문제를 방지한다
        map.put("jobName", job.getName()); // 잡 이름
        if (extraData != null) {
            // 필요 시 추가 데이터를 포함
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
            JobChainingJobListener jobChainingJobListener,
            List<Job> jobBeans,
            @Qualifier("dataSource-stg") DataSource quartzDataSource,
            AutowiringJobFactory autowiringJobFactory,
            QuartzProperties quartzProperties) throws Exception {
        // jobBeans 파라미터는 모든 Job 빈을 조기 로딩하기 위한 것으로 실제로 사용하지 않는다.
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        // Quartz 스케줄러에서 사용할 데이터소스 지정
        factory.setDataSource(quartzDataSource);
        // Quartz 잡 클래스에 스프링 빈 주입을 가능하게 하는 JobFactory 설정
        factory.setJobFactory(autowiringJobFactory);
        // application.yml 설정을 Properties 객체로 변환해 적용
        Properties quartzProps = new Properties();
        quartzProps.putAll(quartzProperties.getProperties());
        factory.setQuartzProperties(quartzProps);
        // true : qrtz_* 테이블에 존재하는 스케줄 정보를 기반으로 덮어쓴다
        // false : DB에 저장된 기존 스케줄 정보를 덮어쓰지 않음 (DB정보 유지)
        factory.setOverwriteExistingJobs(false);

        List<Trigger> triggers = new ArrayList<>();
        List<JobDetail> jobDetails = new ArrayList<>();

        // Quartz에서 관리하는 크론 트리거 정보를 조회한다
        JdbcTemplate jdbcTemplate = new JdbcTemplate(quartzDataSource);
        List<SchedulerJobDto> jobDtos = jdbcTemplate.query(
                "SELECT t.JOB_NAME, c.CRON_EXPRESSION FROM qrtz_triggers t "
                        + "JOIN qrtz_cron_triggers c ON t.TRIGGER_NAME = c.TRIGGER_NAME AND t.TRIGGER_GROUP = c.TRIGGER_GROUP",
                (rs, rowNum) -> new SchedulerJobDto(rs.getString("JOB_NAME"), rs.getString("CRON_EXPRESSION"))
        );
        if (jobDtos.isEmpty()) {
            // DB 정보가 없으면 설정된 jobs 맵을 기반으로 초기 등록
            // application.yml에서 가져온 Map 사용 (잡 이름: 크론 표현식)
            Map<String, String> jobs = schedulerProps.getJobs();            
            LOGGER.info("DB 스케줄 정보가 없어 scheduler.jobs로 초기 등록합니다.");
            LOGGER.info("jobs.entrySet():", jobs.entrySet());

            for (Map.Entry<String, String> entry : jobs.entrySet()) {
                String jobName = entry.getKey();
                String cron = entry.getValue();

                Job job;
                try {
                    job = jobRegistry.getJob(jobName);
                } catch (NoSuchJobException e) {
                    LOGGER.warn("등록되지 않은 잡 '{}'을(를) 건너뜁니다. 사유: {}", jobName, e.getMessage());
                    continue;
                }

                boolean durability = (cron == null || cron.isEmpty());
                JobDetail jobDetail = createJobDetail(job, durability, extraData(jobName));

                if (durability) {
                    jobDetails.add(jobDetail);
                    LOGGER.info("내구성 잡 '{}'을(를) 초기 등록했습니다.", jobName);
                } else {
                    triggers.add(cronTrigger(jobDetail, cron));
                    LOGGER.info("크론 '{}'으로 잡 '{}'을(를) 초기 등록했습니다.", cron, jobName);
                }
            }
        } else {
            // DB에 저장된 스케줄 정보를 사용
            LOGGER.info("DB에서 조회한 스케줄 정보 {}건을 사용합니다.", jobDtos.size());
            for (SchedulerJobDto jobDto : jobDtos) {
                String jobName = jobDto.getJobName();
                String cron = jobDto.getCronExpression();

                Job job;
                try {
                    job = jobRegistry.getJob(jobName);
                } catch (NoSuchJobException e) {
                    LOGGER.warn("등록되지 않은 잡 '{}'을(를) 건너뜁니다. 사유: {}", jobName, e.getMessage());
                    continue;
                }

                boolean durability = (cron == null || cron.isEmpty());
                JobDetail jobDetail = createJobDetail(job, durability, extraData(jobName));

                if (durability) {
                    jobDetails.add(jobDetail);
                } else {
                    triggers.add(cronTrigger(jobDetail, cron));
                }
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
    	/* 추가 데이터 넣는 예시
        if ("insaStgToLocalJob".equals(jobName)) {
            Map<String, Object> extra = new HashMap<>();
            extra.put("sourceSystem", "remote1");
            return extra;
        }
        */
        return null;
    }
}

