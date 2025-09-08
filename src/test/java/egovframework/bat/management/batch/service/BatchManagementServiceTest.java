package egovframework.bat.management.batch.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import egovframework.bat.service.BatchManagementMapper;
import egovframework.bat.service.dto.JobExecutionDto;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.configuration.JobRegistry;

/**
 * BatchManagementService의 기능을 검증하는 단위 테스트.
 */
public class BatchManagementServiceTest {

    private BatchManagementService batchManagementService;

    @Mock
    private BatchManagementMapper batchManagementMapper;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private JobExplorer jobExplorer;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private Job mybatisJob;

    @Mock
    private JobRegistry jobRegistry;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        try {
            when(jobRegistry.getJob("mybatisToMybatisSampleJob")).thenReturn(mybatisJob);
        } catch (NoSuchJobException e) {
            // 예외 발생 시 테스트 실패
            fail("지정된 잡을 찾지 못했습니다: " + e.getMessage());
        }

        batchManagementService = new BatchManagementService(batchManagementMapper,
                jobLauncher, jobExplorer, jobRepository, jobRegistry);
    }

    @Test
    public void getJobNamesReturnsMapperResult() {
        List<String> expected = Arrays.asList("job1", "job2");
        when(batchManagementMapper.selectJobNames()).thenReturn(expected);

        List<String> result = batchManagementService.getJobNames();

        assertEquals(expected, result);
    }

    @Test
    public void getJobExecutionsReturnsMapperResult() {
        JobExecutionDto dto = new JobExecutionDto();
        dto.setId(1L);
        dto.setStatus("COMPLETED");
        when(batchManagementMapper.selectJobExecutions("job")).thenReturn(Arrays.asList(dto));

        List<JobExecution> result = batchManagementService.getJobExecutions("job");

        assertEquals(1, result.size());
        JobExecution execution = result.get(0);
        assertNotNull(execution);
        assertEquals(Long.valueOf(1L), execution.getId());
        assertEquals("COMPLETED", execution.getStatus().toString());
    }

    @Test
    public void getErrorLogsReturnsMapperResult() {
        List<String> logs = Arrays.asList("error1", "error2");
        when(batchManagementMapper.selectErrorLogs(1L)).thenReturn(logs);

        List<String> result = batchManagementService.getErrorLogs(1L);

        assertEquals(logs, result);
    }

    @Test
    public void restartRunsJobThroughLauncher() throws Exception {
        batchManagementService.restart("mybatisToMybatisSampleJob");

        verify(jobLauncher).run(eq(mybatisJob), any(JobParameters.class));
    }

    @Test
    public void stopUpdatesRunningExecution() {
        JobExecution execution = new JobExecution(1L);
        when(jobExplorer.findRunningJobExecutions("mybatisToMybatisSampleJob"))
                .thenReturn(Collections.singleton(execution));

        batchManagementService.stop("mybatisToMybatisSampleJob");

        verify(jobRepository).update(execution);
    }
}

