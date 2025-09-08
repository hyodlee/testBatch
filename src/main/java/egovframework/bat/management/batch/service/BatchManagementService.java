package egovframework.bat.management.batch.service;

import java.util.List;

import egovframework.bat.service.BatchManagementMapper;

import egovframework.bat.service.dto.JobExecutionDto;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 배치 잡 관리 기능을 제공하는 서비스.
 */
@Service
public class BatchManagementService {

    /** 배치 메타데이터 조회를 위한 매퍼 */
    private final BatchManagementMapper batchManagementMapper;

    /** 잡 실행을 위한 런처 */
    private final JobLauncher jobLauncher;

    /** 실행 중인 잡 조회를 위한 탐색기 */
    private final JobExplorer jobExplorer;

    /** 실행 상태 갱신을 위한 레포지토리 */
    private final JobRepository jobRepository;

    /** 등록된 잡을 조회하기 위한 레지스트리 */
    private final JobRegistry jobRegistry;

    @Autowired
    public BatchManagementService(BatchManagementMapper batchManagementMapper,
            JobLauncher jobLauncher, JobExplorer jobExplorer, JobRepository jobRepository,
            JobRegistry jobRegistry) {
        this.batchManagementMapper = batchManagementMapper;
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.jobRepository = jobRepository;
        this.jobRegistry = jobRegistry;
    }

    /**
     * 등록된 배치 잡 이름 목록을 반환한다.
     *
     * @return 잡 이름 목록
     */
    public List<String> getJobNames() {
        return batchManagementMapper.selectJobNames();
    }

    /**
     * 특정 잡의 실행 이력을 조회한다.
     *
     * @param jobName 잡 이름
     * @return 실행 이력 목록
     */
    public List<JobExecution> getJobExecutions(String jobName) {
        List<JobExecutionDto> dtos = batchManagementMapper.selectJobExecutions(jobName);
        return dtos.stream().map(this::toJobExecution).collect(Collectors.toList());
    }

    /**
     * JobExecutionDto를 JobExecution으로 변환한다.
     *
     * @param dto 변환할 DTO
     * @return 변환된 JobExecution
     */
    private JobExecution toJobExecution(JobExecutionDto dto) {
        JobExecution jobExecution = new JobExecution(dto.getId());
        if (dto.getStartTime() != null) {
            jobExecution.setStartTime(Date.from(dto.getStartTime().atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (dto.getEndTime() != null) {
            jobExecution.setEndTime(Date.from(dto.getEndTime().atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (dto.getStatus() != null) {
            jobExecution.setStatus(BatchStatus.valueOf(dto.getStatus()));
        }
        jobExecution.setExitStatus(new ExitStatus(dto.getExitCode(), dto.getExitDescription()));
        if (dto.getCreateTime() != null) {
            jobExecution.setCreateTime(Date.from(dto.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (dto.getLastUpdated() != null) {
            jobExecution.setLastUpdated(Date.from(dto.getLastUpdated().atZone(ZoneId.systemDefault()).toInstant()));
        }
        return jobExecution;
    }

    /**
     * 특정 잡 실행에 대한 에러 로그를 조회한다.
     *
     * @param jobExecutionId 잡 실행 ID
     * @return 에러 로그 목록
     */
    public List<String> getErrorLogs(Long jobExecutionId) {
        return batchManagementMapper.selectErrorLogs(jobExecutionId);
    }

    /**
     * 주어진 이름의 잡을 재실행한다.
     *
     * @param jobName 재실행할 잡 이름
     * @throws Exception 잡 실행 중 예외 발생 시
     */
    public void restart(String jobName) throws Exception {
        Job job = jobRegistry.getJob(jobName);
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(job, params);
    }

    /**
     * 실행 중인 잡을 중지한다.
     *
     * @param jobName 중지할 잡 이름
     */
    public void stop(String jobName) {
        jobExplorer.findRunningJobExecutions(jobName).forEach(execution -> {
            execution.stop();
            jobRepository.update(execution);
        });
    }
}

