package egovframework.bat.repository.dto;

import lombok.Data;

/**
 * BATCH_JOB_INSTANCE 정보를 담는 DTO.
 */
@Data
public class BatchJobInstanceDto {
    // 잡 인스턴스 ID
    private Long jobInstanceId;
    // 잡 이름
    private String jobName;
}

