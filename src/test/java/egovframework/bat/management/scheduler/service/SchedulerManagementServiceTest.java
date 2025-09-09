package egovframework.bat.management.scheduler.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import org.quartz.*;
import org.quartz.jobs.NoOpJob;

import egovframework.bat.management.scheduler.dto.ScheduledJobDto;
import egovframework.bat.management.scheduler.exception.TriggerNotFoundException;

/**
 * SchedulerManagementService의 내구성 필드 처리를 검증한다.
 */
public class SchedulerManagementServiceTest {

    private SchedulerManagementService schedulerManagementService;

    @Mock
    private Scheduler scheduler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        schedulerManagementService = new SchedulerManagementService(scheduler);
    }

    @Test
    public void listJobsIncludesDurableFlag() throws Exception {
        JobKey jobKey = JobKey.jobKey("testJob");
        Set<JobKey> jobKeys = new HashSet<>();
        jobKeys.add(jobKey);
        when(scheduler.getJobKeys(any())).thenReturn(jobKeys);

        JobDetail jobDetail = JobBuilder.newJob(NoOpJob.class)
                .withIdentity(jobKey)
                .storeDurably(true)
                .build();
        when(scheduler.getJobDetail(jobKey)).thenReturn(jobDetail);

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("testJobTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ?"))
                .build();
        // 트리거 리스트 모킹
        doReturn(Collections.singletonList(trigger)).when(scheduler).getTriggersOfJob(jobKey);
        when(scheduler.getTriggerState(trigger.getKey())).thenReturn(Trigger.TriggerState.NORMAL);

        List<ScheduledJobDto> jobs = schedulerManagementService.listJobs();
        assertEquals(1, jobs.size());
        assertTrue(jobs.get(0).isDurable());
    }

    @Test
    public void getJobReturnsDurableFlag() throws Exception {
        JobKey jobKey = JobKey.jobKey("testJob");
        when(scheduler.checkExists(jobKey)).thenReturn(true);

        JobDetail jobDetail = JobBuilder.newJob(NoOpJob.class)
                .withIdentity(jobKey)
                .storeDurably(false)
                .build();
        when(scheduler.getJobDetail(jobKey)).thenReturn(jobDetail);

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("testJobTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ?"))
                .build();
        // 트리거 리스트 모킹
        doReturn(Collections.singletonList(trigger)).when(scheduler).getTriggersOfJob(jobKey);
        when(scheduler.getTriggerState(trigger.getKey())).thenReturn(Trigger.TriggerState.NORMAL);

        ScheduledJobDto job = schedulerManagementService.getJob("testJob");
        assertNotNull(job);
        assertFalse(job.isDurable());
    }

    @Test
    public void updateJobCronWithGroupChangesCron() throws Exception {
        String jobName = "testJob";
        String group = "quartz-batch";

        JobDetail jobDetail = JobBuilder.newJob(NoOpJob.class)
                .withIdentity(jobName)
                .storeDurably(false)
                .build();
        when(scheduler.getJobDetail(JobKey.jobKey(jobName))).thenReturn(jobDetail);

        TriggerKey triggerKey = TriggerKey.triggerKey(jobName + "Trigger", group);
        CronTrigger oldTrigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?"))
                .build();
        when(scheduler.getTrigger(triggerKey)).thenReturn(oldTrigger);

        ArgumentCaptor<Trigger> captor = ArgumentCaptor.forClass(Trigger.class);

        schedulerManagementService.updateJobCron(jobName, "0/5 * * * * ?", group);

        verify(scheduler).rescheduleJob(eq(triggerKey), captor.capture());
        CronTrigger newTrigger = (CronTrigger) captor.getValue();
        assertEquals("0/5 * * * * ?", newTrigger.getCronExpression());
    }

    @Test
    public void updateJobCronFindsTriggerInAnotherGroup() throws Exception {
        String jobName = "testJob";
        String group = "quartz-batch";
        String otherGroup = "other-group";

        JobDetail jobDetail = JobBuilder.newJob(NoOpJob.class)
                .withIdentity(jobName)
                .storeDurably(false)
                .build();
        when(scheduler.getJobDetail(JobKey.jobKey(jobName))).thenReturn(jobDetail);

        TriggerKey initialTriggerKey = TriggerKey.triggerKey(jobName + "Trigger", group);
        when(scheduler.getTrigger(initialTriggerKey)).thenReturn(null);

        TriggerKey foundTriggerKey = TriggerKey.triggerKey(jobName + "Trigger", otherGroup);
        CronTrigger oldTrigger = TriggerBuilder.newTrigger()
                .withIdentity(foundTriggerKey)
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?"))
                .build();
        doReturn(Collections.singletonList(oldTrigger)).when(scheduler).getTriggersOfJob(JobKey.jobKey(jobName));

        ArgumentCaptor<Trigger> captor = ArgumentCaptor.forClass(Trigger.class);

        schedulerManagementService.updateJobCron(jobName, "0/5 * * * * ?", group);

        verify(scheduler).rescheduleJob(eq(foundTriggerKey), captor.capture());
        CronTrigger newTrigger = (CronTrigger) captor.getValue();
        assertEquals("0/5 * * * * ?", newTrigger.getCronExpression());
    }

    @Test(expected = TriggerNotFoundException.class)
    public void updateJobCronThrowsTriggerNotFoundExceptionWhenMissing() throws Exception {
        String jobName = "missingJob";
        String group = "quartz-batch";

        JobDetail jobDetail = JobBuilder.newJob(NoOpJob.class)
                .withIdentity(jobName)
                .storeDurably(false)
                .build();
        when(scheduler.getJobDetail(JobKey.jobKey(jobName))).thenReturn(jobDetail);

        TriggerKey triggerKey = TriggerKey.triggerKey(jobName + "Trigger", group);
        when(scheduler.getTrigger(triggerKey)).thenReturn(null);
        doReturn(Collections.emptyList()).when(scheduler).getTriggersOfJob(JobKey.jobKey(jobName));

        schedulerManagementService.updateJobCron(jobName, "0/5 * * * * ?", group);
    }
}
