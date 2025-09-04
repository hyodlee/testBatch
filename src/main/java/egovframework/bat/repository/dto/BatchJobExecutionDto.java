package egovframework.bat.repository.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * BATCH_JOB_EXECUTION 정보를 담는 DTO.
 */
@Data
public class BatchJobExecutionDto {
    // 잡 실행 ID
    private Long jobExecutionId;
    // 잡 인스턴스 ID
    private Long jobInstanceId;
    // 실행 시작 시간
    private LocalDateTime startTime;
    // 실행 종료 시간
    private LocalDateTime endTime;
    // 실행 상태
    private String status;
    // 잡 이름
    private String jobName;
}

