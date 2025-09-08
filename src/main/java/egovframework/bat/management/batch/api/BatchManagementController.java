package egovframework.bat.management.batch.api;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.bat.repository.dto.BatchJobExecutionDto;
import egovframework.bat.management.batch.service.BatchManagementService;

/**
 * 배치 잡 조회와 제어를 위한 REST 컨트롤러.
 */
@RestController
@RequestMapping("/api/management/batch")
@RequiredArgsConstructor
public class BatchManagementController {

    /** 배치 관리 서비스를 주입 */
    private final BatchManagementService batchManagementService;

    /**
     * 등록된 배치 잡 이름 목록을 반환한다.
     *
     * @return 잡 이름 목록
     */
    @GetMapping("/jobs")
    public ResponseEntity<List<String>> getJobNames() {
        return ResponseEntity.ok(batchManagementService.getJobNames());
    }

    /**
     * 특정 잡의 실행 이력을 반환한다.
     *
     * @param jobName 잡 이름
     * @return 실행 이력 목록
     */
    @GetMapping("/jobs/{jobName}/executions")
    public ResponseEntity<List<BatchJobExecutionDto>> getExecutions(@PathVariable String jobName) {
        List<JobExecution> executions = batchManagementService.getJobExecutions(jobName);
        List<BatchJobExecutionDto> dtos = executions.stream().map(je -> {
            BatchJobExecutionDto dto = new BatchJobExecutionDto();
            dto.setJobExecutionId(je.getId());
            if (je.getJobInstance() != null) {
                dto.setJobInstanceId(je.getJobInstance().getInstanceId());
            }
            if (je.getStartTime() != null) {
                dto.setStartTime(LocalDateTime.ofInstant(je.getStartTime().toInstant(), ZoneId.systemDefault()));
            }
            if (je.getEndTime() != null) {
                dto.setEndTime(LocalDateTime.ofInstant(je.getEndTime().toInstant(), ZoneId.systemDefault()));
            }
            if (je.getStatus() != null) {
                dto.setStatus(je.getStatus().toString());
            }
            dto.setJobName(jobName);
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * 특정 잡 실행의 에러 로그를 반환한다.
     *
     * @param jobExecutionId 잡 실행 ID
     * @return 에러 로그 목록
     */
    @GetMapping("/executions/{jobExecutionId}/errors")
    public ResponseEntity<List<String>> getErrorLogs(@PathVariable Long jobExecutionId) {
        return ResponseEntity.ok(batchManagementService.getErrorLogs(jobExecutionId));
    }

    /**
     * 실패한 잡을 재실행한다.
     *
     * @param jobName 잡 이름
     * @return 처리 결과
     * @throws Exception 재실행 중 예외 발생 시
     */
    @PostMapping("/jobs/{jobName}/restart")
    public ResponseEntity<Void> restart(@PathVariable String jobName) throws Exception {
        batchManagementService.restart(jobName);
        return ResponseEntity.ok().build();
    }

    /**
     * 실행 중인 잡을 중지한다.
     *
     * @param jobName 잡 이름
     * @return 처리 결과
     */
    @PostMapping("/jobs/{jobName}/stop")
    public ResponseEntity<Void> stop(@PathVariable String jobName) {
        batchManagementService.stop(jobName);
        return ResponseEntity.ok().build();
    }
}

