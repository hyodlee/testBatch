package egovframework.bat.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobOperator;

/**
 * BatchManagementService의 기능을 검증하는 단위 테스트.
 */
public class BatchManagementServiceTest {

    private BatchManagementService batchManagementService;

    @Mock
    private BatchManagementMapper batchManagementMapper;

    @Mock
    private JobOperator jobOperator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        batchManagementService = new BatchManagementService(batchManagementMapper, jobOperator);
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
        List<JobExecution> executions = Arrays.asList(new JobExecution(1L), new JobExecution(2L));
        when(batchManagementMapper.selectJobExecutions("job")).thenReturn(executions);

        List<JobExecution> result = batchManagementService.getJobExecutions("job");

        assertEquals(executions, result);
    }

    @Test
    public void getErrorLogsReturnsMapperResult() {
        List<String> logs = Arrays.asList("error1", "error2");
        when(batchManagementMapper.selectErrorLogs(1L)).thenReturn(logs);

        List<String> result = batchManagementService.getErrorLogs(1L);

        assertEquals(logs, result);
    }

    @Test
    public void restartDelegatesToJobOperator() throws Exception {
        batchManagementService.restart(1L);

        verify(jobOperator).restart(1L);
    }

    @Test
    public void stopDelegatesToJobOperator() throws Exception {
        batchManagementService.stop(1L);

        verify(jobOperator).stop(1L);
    }
}

