package egovframework.bat.management;

import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

/**
 * Quartz 스케줄러를 제어하기 위한 서비스.
 */
@Service
@RequiredArgsConstructor
public class SchedulerManagementService {

    /** Quartz 스케줄러 */
    private final Scheduler scheduler;

    /**
     * 새로운 잡을 추가한다.
     *
     * @param jobName        잡 이름
     * @param jobClassName   실행할 Job 클래스의 이름
     * @param cronExpression 실행 주기를 나타내는 크론 표현식
     * @throws ClassNotFoundException 클래스 로딩 실패 시 발생
     * @throws SchedulerException     스케줄러 작업 실패 시 발생
     */
    public void addJob(String jobName, String jobClassName, String cronExpression)
            throws ClassNotFoundException, SchedulerException {
        @SuppressWarnings("unchecked")
        Class<? extends Job> jobClass = (Class<? extends Job>) Class.forName(jobClassName);
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobName)
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "Trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * 등록된 잡을 일시 중지한다.
     *
     * @param jobName 잡 이름
     * @throws SchedulerException 스케줄러 작업 실패 시 발생
     */
    public void pauseJob(String jobName) throws SchedulerException {
        scheduler.pauseJob(JobKey.jobKey(jobName));
    }

    /**
     * 일시 중지된 잡을 재개한다.
     *
     * @param jobName 잡 이름
     * @throws SchedulerException 스케줄러 작업 실패 시 발생
     */
    public void resumeJob(String jobName) throws SchedulerException {
        scheduler.resumeJob(JobKey.jobKey(jobName));
    }

    /**
     * 등록된 잡을 삭제한다.
     *
     * @param jobName 잡 이름
     * @throws SchedulerException 스케줄러 작업 실패 시 발생
     */
    public void deleteJob(String jobName) throws SchedulerException {
        scheduler.deleteJob(JobKey.jobKey(jobName));
    }
}

