package egovframework.bat.management;

import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import egovframework.bat.management.dto.ScheduledJobDto;
import egovframework.bat.management.exception.InvalidCronExpressionException;

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
        // 크론 정보는 Quartz 테이블에 자동으로 저장된다
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

    /**
     * 등록된 잡의 크론 표현식을 변경한다.
     *
     * @param jobName        잡 이름
     * @param cronExpression 새 크론 표현식
     * @throws SchedulerException 스케줄러 작업 실패 시 발생
     */
    public void updateJobCron(String jobName, String cronExpression) throws SchedulerException {
        // 크론 표현식 유효성 검사
        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new InvalidCronExpressionException("유효하지 않은 크론 표현식입니다: " + cronExpression);
        }

        TriggerKey triggerKey = TriggerKey.triggerKey(jobName + "Trigger");
        Trigger newTrigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
        scheduler.rescheduleJob(triggerKey, newTrigger);
        // 변경된 크론 정보도 Quartz 테이블에 자동 반영된다
    }

    /**
     * 등록된 모든 잡의 정보를 조회한다.
     *
     * @return 잡 정보 목록
     * @throws SchedulerException 스케줄러 작업 실패 시 발생
     */
    public List<ScheduledJobDto> listJobs() throws SchedulerException {
        List<ScheduledJobDto> jobs = new ArrayList<>();
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyGroup())) {
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            String cronExpression = "";
            String status = "";
            boolean durable = false;
            if (!triggers.isEmpty()) {
                Trigger trigger = triggers.get(0);
                if (trigger instanceof CronTrigger) {
                    cronExpression = ((CronTrigger) trigger).getCronExpression();
                }
                status = scheduler.getTriggerState(trigger.getKey()).name();
            }
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if (jobDetail != null) {
                durable = jobDetail.isDurable();
            }
            jobs.add(new ScheduledJobDto(jobKey.getName(), cronExpression, status, durable));
        }
        return jobs;
    }

    /**
     * 특정 잡의 정보를 조회한다.
     *
     * @param jobName 잡 이름
     * @return 잡 정보, 존재하지 않을 경우 null
     * @throws SchedulerException 스케줄러 작업 실패 시 발생
     */
    public ScheduledJobDto getJob(String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName);
        if (!scheduler.checkExists(jobKey)) {
            return null;
        }
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
        String cronExpression = "";
        String status = "";
        boolean durable = false;
        if (!triggers.isEmpty()) {
            Trigger trigger = triggers.get(0);
            if (trigger instanceof CronTrigger) {
                cronExpression = ((CronTrigger) trigger).getCronExpression();
            }
            status = scheduler.getTriggerState(trigger.getKey()).name();
        }
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        if (jobDetail != null) {
            durable = jobDetail.isDurable();
        }
        return new ScheduledJobDto(jobName, cronExpression, status, durable);
    }
}

