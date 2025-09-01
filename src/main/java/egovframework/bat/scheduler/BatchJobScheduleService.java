package egovframework.bat.scheduler;

import java.util.List;

import javax.annotation.PostConstruct;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

/**
 * DB에서 크론 표현식을 조회하여 Quartz 스케줄을 등록/재등록하는 서비스.
 */
@Service
public class BatchJobScheduleService {

    private final Scheduler scheduler;
    private final ApplicationContext applicationContext;
    private final BatchJobScheduleRepository repository;

    public BatchJobScheduleService(SchedulerFactoryBean schedulerFactoryBean,
                                   ApplicationContext applicationContext,
                                   BatchJobScheduleRepository repository) {
        this.scheduler = schedulerFactoryBean.getScheduler();
        this.applicationContext = applicationContext;
        this.repository = repository;
    }

    /**
     * 애플리케이션 시작 시 스케줄을 등록한다.
     */
    @PostConstruct
    public void init() throws SchedulerException {
        refresh();
    }

    /**
     * DB 내용을 기반으로 스케줄을 모두 재등록한다.
     * 변경된 크론 표현식을 반영하기 위해 사용한다.
     */
    public void refresh() throws SchedulerException {
        // 기존 스케줄 초기화
        scheduler.clear();

        // 트리거 없이 등록해야 하는 잡 추가
        scheduler.addJob(applicationContext.getBean("insaStgToLocalJobDetail", JobDetail.class), true);
        scheduler.addJob(applicationContext.getBean("erpStgToLocalJobDetail", JobDetail.class), true);

        List<BatchJobSchedule> schedules = repository.findAll();
        for (BatchJobSchedule schedule : schedules) {
            JobDetail jobDetail = applicationContext.getBean(schedule.getJobName(), JobDetail.class);
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .forJob(jobDetail)
                    .withIdentity(schedule.getJobName() + "Trigger")
                    .withSchedule(CronScheduleBuilder.cronSchedule(schedule.getCronExpression()))
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
        }
    }
}
