package egovframework.bat.management.batch.service;

import java.util.List;

import egovframework.bat.service.BatchManagementMapper;

import egovframework.bat.service.dto.JobExecutionDto;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 배치 잡 관리 기능을 제공하는 서비스.
 */
@Slf4j
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
        try {
            Job job = jobRegistry.getJob(jobName);
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            // 잡 이름과 파라미터 로그 출력
            log.info("잡 재실행 요청 - 이름: {}, 파라미터: {}", jobName, params);
            // 실행 전 상태 기록
            log.info("잡 실행 시작");
            jobLauncher.run(job, params);
            // 실행 후 상태 기록
            log.info("잡 실행 완료");
        } catch (Exception e) {
            // 예외 발생 시 로그 남기고 다시 던짐
            log.error("잡 재실행 중 예외 발생 - 잡 이름: {}", jobName, e);
            throw e;
        }
    }

    /**
     * 실행 중인 잡을 중지한다.
     *
     * @param jobName 중지할 잡 이름
     */
    public void stop(String jobName) {
        try {
            Set<JobExecution> executions = jobExplorer.findRunningJobExecutions(jobName);
            // 조회된 실행 건수 로그
            log.info("중지 대상 실행 건수: {}", executions.size());
            executions.forEach(execution -> {
                try {
                    execution.stop();
                    jobRepository.update(execution);
                    // 각 실행의 중지 성공 여부 로그
                    log.debug("잡 실행 {} 중지 성공", execution.getId());
                } catch (Exception e) {
                    // 실행 중 예외 발생 시 로그
                    log.error("잡 실행 {} 중지 실패", execution.getId(), e);
                }
            });
        } catch (Exception e) {
            // 전체 중지 과정에서 예외 발생 시 로그 후 예외 전달
            log.error("잡 중지 처리 중 예외 발생 - 잡 이름: {}", jobName, e);
            throw new IllegalStateException("잡 중지 실패: " + jobName, e);
        }
    }
}

