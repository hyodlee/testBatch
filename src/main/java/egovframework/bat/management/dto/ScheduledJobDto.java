package egovframework.bat.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 스케줄된 잡 정보를 전달하기 위한 DTO.
 */
@Data
@AllArgsConstructor
public class ScheduledJobDto {
    /** 잡 이름 */
    private String jobName;
    /** 크론 표현식 */
    private String cronExpression;
    /** 현재 상태 */
    private String status;
}
