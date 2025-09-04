package egovframework.bat.repository.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * BATCH_STEP_EXECUTION 정보를 담는 DTO.
 */
@Data
public class BatchStepExecutionDto {
    // 스텝 실행 ID
    private Long stepExecutionId;
    // 잡 실행 ID
    private Long jobExecutionId;
    // 스텝 이름
    private String stepName;
    // 실행 시작 시간
    private LocalDateTime startTime;
    // 실행 종료 시간
    private LocalDateTime endTime;
    // 실행 상태
    private String status;
}

