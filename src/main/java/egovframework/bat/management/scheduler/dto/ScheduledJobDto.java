package egovframework.bat.management.scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 스케줄된 잡 정보를 전달하기 위한 DTO.
 *
 * <p>잡 이름, 그룹, 크론 표현식, 상태, 내구성 여부를 포함한다.</p>
 */
@Data
@AllArgsConstructor
public class ScheduledJobDto {
    /** 잡 이름 */
    private String jobName;
    /** 잡 그룹 */
    private String jobGroup;
    /** 크론 표현식 */
    private String cronExpression;
    /** 현재 상태 */
    private String status;
    /** 내구성 여부 */
    private boolean durable;
}
