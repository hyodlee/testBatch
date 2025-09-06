package egovframework.bat.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 스케줄러 잡 설정 정보를 담는 DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerJobDto {
    /** 잡 이름 */
    private String jobName;
    /** 크론 표현식 */
    private String cronExpression;
}
