package egovframework.bat.management.scheduler.service;

import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.TimeZone;

import egovframework.bat.management.scheduler.dto.ScheduledJobDto;
import egovframework.bat.management.scheduler.exception.InvalidCronExpressionException;
import egovframework.bat.management.scheduler.exception.DurableJobCronUpdateNotAllowedException;
import egovframework.bat.management.scheduler.exception.DurableJobPauseResumeNotAllowedException;

/**
 * Quartz 스케줄러를 제어하기 위한 서비스.
 */
@Service
@RequiredArgsConstructor
public class SchedulerManagementService {

    /** 로깅을 위한 로거 */
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerManagementService.class);

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
        JobKey jobKey = JobKey.jobKey(jobName);
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        // 내구성 잡은 일시 중지할 수 없도록 예외 처리
        if (jobDetail != null && jobDetail.isDurable()) {
            throw new DurableJobPauseResumeNotAllowedException(
                    "내구성 잡은 일시 중지할 수 없습니다: " + jobName);
        }
        scheduler.pauseJob(jobKey);
    }

    /**
     * 일시 중지된 잡을 재개한다.
     *
     * @param jobName 잡 이름
     * @throws SchedulerException 스케줄러 작업 실패 시 발생
     */
    public void resumeJob(String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName);
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        // 내구성 잡은 재개할 수 없도록 예외 처리
        if (jobDetail != null && jobDetail.isDurable()) {
            throw new DurableJobPauseResumeNotAllowedException(
                    "내구성 잡은 재개할 수 없습니다: " + jobName);
        }
        scheduler.resumeJob(jobKey);
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
        updateJobCron(jobName, cronExpression, "quartz-batch");
    }

    /**
     * 등록된 잡의 크론 표현식을 변경한다.
     *
     * @param jobName        잡 이름
     * @param cronExpression 새 크론 표현식
     * @param group          트리거 그룹, 기본값은 "quartz-batch"
     * @throws SchedulerException 스케줄러 작업 실패 시 발생
     */
    public void updateJobCron(String jobName, String cronExpression, String group) throws SchedulerException {
        LOGGER.debug("잡 {} 크론 변경 요청: {}", jobName, cronExpression);
        JobDetail jobDetail = scheduler.getJobDetail(JobKey.jobKey(jobName));
        LOGGER.debug("JobDetail: {}, isDurable: {}", jobDetail, jobDetail != null ? jobDetail.isDurable() : null);
        if (jobDetail != null && jobDetail.isDurable()) {
            throw new DurableJobCronUpdateNotAllowedException(
                    "내구성 잡은 크론 표현식을 변경할 수 없습니다: " + jobName);
        }

        // 크론 표현식 유효성 검사
        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new InvalidCronExpressionException("유효하지 않은 크론 표현식입니다: " + cronExpression);
        }
        LOGGER.debug("크론 표현식 유효성 통과");

        TriggerKey triggerKey = TriggerKey.triggerKey(jobName + "Trigger", group);
        // 기존 트리거 조회
        Trigger oldTrigger = scheduler.getTrigger(triggerKey);
        if (oldTrigger == null) {
            LOGGER.info("기존 트리거를 찾지 못했습니다: {}", triggerKey);
            throw new SchedulerException("Trigger not found: " + triggerKey);
        } else {
            LOGGER.info("기존 트리거를 찾았습니다: {}", triggerKey);
        }

        // 기존 트리거의 시작/종료 시간을 유지
        Date startTime = oldTrigger.getStartTime();
        Date endTime = oldTrigger.getEndTime();

        // 미스파이어 시 아무 작업도 하지 않도록 설정하고, 필요 시 기존 시간대를 반영
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression)
                .withMisfireHandlingInstructionDoNothing();

        if (oldTrigger instanceof CronTrigger) {
            // 기존 트리거가 CronTrigger인 경우 기존 크론 표현식과 시간대를 로그로 남김
            String oldCron = ((CronTrigger) oldTrigger).getCronExpression();
            LOGGER.info("기존 크론 표현식: {}", oldCron);
            TimeZone timeZone = ((CronTrigger) oldTrigger).getTimeZone();
            if (timeZone != null) {
                cronScheduleBuilder = cronScheduleBuilder.inTimeZone(TimeZone.getTimeZone(timeZone.getID()));
            }
        }

        // 새로 적용할 크론 표현식을 로그로 남김
        LOGGER.info("새 크론 표현식: {}", cronExpression);

        TriggerBuilder<?> triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(cronScheduleBuilder);

        if (startTime != null) {
            triggerBuilder.startAt(startTime);
        }
        if (endTime != null) {
            triggerBuilder.endAt(endTime);
        }

        Trigger newTrigger = triggerBuilder.build();
        LOGGER.debug("TriggerKey {}에 대해 크론 {}으로 재스케줄링 시도", triggerKey, cronExpression);
        scheduler.rescheduleJob(triggerKey, newTrigger);
        LOGGER.info("잡 {}의 크론을 {}로 변경 완료", jobName, cronExpression);
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

