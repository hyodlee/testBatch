package egovframework.bat.api;

import egovframework.bat.service.BatchManagementService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 배치 잡과 실행 정보를 조회/관리하기 위한 REST 컨트롤러.
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchManagementController {

    /** 배치 관리 서비스 */
    private final BatchManagementService batchManagementService;

    /** 배치 메타데이터 조회를 위한 JobExplorer */
    private final JobExplorer jobExplorer;

    /**
     * 등록된 잡 목록과 최근 실행 상태를 반환한다.
     *
     * @return 잡 이름과 최근 실행 상태 목록
     */
    @GetMapping("/jobs")
    public List<Map<String, Object>> getJobs() {
        return batchManagementService.getJobNames().stream().map(jobName -> {
            List<JobExecution> executions = batchManagementService.getJobExecutions(jobName);
            String status = executions.isEmpty() ? null : executions.get(0).getStatus().toString();
            Map<String, Object> map = new HashMap<>();
            map.put("jobName", jobName);
            map.put("lastStatus", status);
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 특정 잡의 실행 이력을 조회한다.
     *
     * @param jobName 잡 이름
     * @return 실행 이력 목록
     */
    @GetMapping("/jobs/{jobName}/executions")
    public List<JobExecution> getJobExecutions(@PathVariable String jobName) {
        return batchManagementService.getJobExecutions(jobName);
    }

    /**
     * 단일 잡 실행의 상세 정보와 스텝 정보를 반환한다.
     *
     * @param execId 잡 실행 ID
     * @return 잡 실행 및 스텝 정보
     */
    @GetMapping("/executions/{execId}")
    public ResponseEntity<Map<String, Object>> getExecutionDetail(@PathVariable Long execId) {
        JobExecution execution = jobExplorer.getJobExecution(execId);
        if (execution == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("execution", execution);
        map.put("steps", execution.getStepExecutions());
        return ResponseEntity.ok(map);
    }

    /**
     * 에러 로그와 ERP 실패 로그를 조회한다.
     *
     * @param jobExecutionId 조회할 잡 실행 ID
     * @return 통합 에러 로그 목록
     */
    @GetMapping("/error-log")
    public List<String> getErrorLogs(@RequestParam("jobExecutionId") Long jobExecutionId) {
        return batchManagementService.getErrorLogs(jobExecutionId);
    }

    /**
     * 실패한 잡 실행을 재시작한다.
     *
     * @param execId 재실행할 잡 실행 ID
     * @return 성공 여부
     * @throws Exception JobOperator에서 발생한 예외
     */
    @PostMapping("/executions/{execId}/restart")
    public ResponseEntity<Void> restart(@PathVariable Long execId) throws Exception {
        batchManagementService.restart(execId);
        return ResponseEntity.ok().build();
    }

    /**
     * 실행 중인 잡을 중지한다.
     *
     * @param execId 중지할 잡 실행 ID
     * @return 성공 여부
     * @throws Exception JobOperator에서 발생한 예외
     */
    @DeleteMapping("/executions/{execId}")
    public ResponseEntity<Void> stop(@PathVariable Long execId) throws Exception {
        batchManagementService.stop(execId);
        return ResponseEntity.noContent().build();
    }
}

