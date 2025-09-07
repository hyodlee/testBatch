package egovframework.bat.management;

import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import egovframework.bat.management.dto.ScheduledJobDto;
import egovframework.bat.management.dto.CronRequest;

/**
 * Quartz 스케줄러 제어를 위한 REST 컨트롤러.
 */
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerManagementController {

    /** 스케줄러 관리 서비스 */
    private final SchedulerManagementService schedulerManagementService;

    /**
     * 새로운 잡을 추가한다.
     *
     * @param jobName        잡 이름
     * @param jobClass       실행할 Job 클래스의 이름
     * @param cronExpression 크론 표현식
     * @return 처리 결과
     * @throws Exception 스케줄러 작업 실패 시 발생
     */
    @PostMapping("/jobs")
    public ResponseEntity<Void> addJob(
            @RequestParam String jobName,
            @RequestParam String jobClass,
            @RequestParam String cronExpression) throws Exception {
        schedulerManagementService.addJob(jobName, jobClass, cronExpression);
        return ResponseEntity.ok().build();
    }

    /**
     * 지정한 잡을 일시 중지한다.
     *
     * @param jobName 잡 이름
     * @return 처리 결과
     * @throws SchedulerException 스케줄러 작업 실패 시 발생
     */
    @PostMapping("/jobs/{jobName}/pause")
    public ResponseEntity<Void> pauseJob(@PathVariable String jobName) throws SchedulerException {
        schedulerManagementService.pauseJob(jobName);
        return ResponseEntity.ok().build();
    }

    /**
     * 일시 중지된 잡을 재개한다.
     *
     * @param jobName 잡 이름
     * @return 처리 결과
     * @throws SchedulerException 스케줄러 작업 실패 시 발생
     */
    @PostMapping("/jobs/{jobName}/resume")
    public ResponseEntity<Void> resumeJob(@PathVariable String jobName) throws SchedulerException {
        schedulerManagementService.resumeJob(jobName);
        return ResponseEntity.ok().build();
    }

    /**
     * 등록된 잡을 삭제한다.
     *
     * @param jobName 잡 이름
     * @return 처리 결과
     * @throws SchedulerException 스케줄러 작업 실패 시 발생
     */
    @DeleteMapping("/jobs/{jobName}")
    public ResponseEntity<Void> deleteJob(@PathVariable String jobName) throws SchedulerException {
        schedulerManagementService.deleteJob(jobName);
        return ResponseEntity.ok().build();
    }

    /**
     * 등록된 잡의 크론 표현식을 변경한다.
     * 프런트엔드는 {"cronExpression":"0 0/2 * * * ?"} 형태의 JSON으로 요청한다.
     *
     * @param jobName 잡 이름
     * @param request 크론 표현식 요청 DTO
     * @return 처리 결과
     * @throws SchedulerException 스케줄러 작업 실패 시 발생
     */
    @PutMapping("/jobs/{jobName}")
    public ResponseEntity<Void> updateJobCron(@PathVariable String jobName,
            @RequestBody CronRequest request) throws SchedulerException {
        schedulerManagementService.updateJobCron(jobName, request.getCronExpression());
        return ResponseEntity.ok().build();
    }

    /**
     * 등록된 모든 잡의 정보를 조회한다.
     *
     * @return 잡 정보 목록
     * @throws SchedulerException 스케줄러 작업 실패 시 발생
     */
    @GetMapping("/jobs")
    public ResponseEntity<List<ScheduledJobDto>> listJobs() throws SchedulerException {
        return ResponseEntity.ok(schedulerManagementService.listJobs());
    }

    /**
     * 특정 잡의 정보를 조회한다.
     *
     * @param jobName 잡 이름
     * @return 잡 정보
     * @throws SchedulerException 스케줄러 작업 실패 시 발생
     */
    @GetMapping("/jobs/{jobName}")
    public ResponseEntity<ScheduledJobDto> getJob(@PathVariable String jobName) throws SchedulerException {
        ScheduledJobDto job = schedulerManagementService.getJob(jobName);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(job);
    }
}

