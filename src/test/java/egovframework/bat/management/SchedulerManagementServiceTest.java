package egovframework.bat.management;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.*;
import org.quartz.jobs.NoOpJob;

import egovframework.bat.management.dto.ScheduledJobDto;

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

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("testJobTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ?"))
                .build();
        when(scheduler.getTriggersOfJob(jobKey)).thenReturn(Collections.singletonList(trigger));
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

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("testJobTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ?"))
                .build();
        when(scheduler.getTriggersOfJob(jobKey)).thenReturn(Collections.singletonList(trigger));
        when(scheduler.getTriggerState(trigger.getKey())).thenReturn(Trigger.TriggerState.NORMAL);

        ScheduledJobDto job = schedulerManagementService.getJob("testJob");
        assertNotNull(job);
        assertFalse(job.isDurable());
    }
}
