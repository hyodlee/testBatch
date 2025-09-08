package egovframework.bat.management.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 배치 작업 진행 상황을 전달하기 위한 DTO.
 */
@Data
@AllArgsConstructor
public class JobProgress {
    /** 작업 이름 */
    private String jobName;
    /** 현재 상태 */
    private String status;
}
