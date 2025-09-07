package egovframework.bat.management.dto;

import lombok.Data;

/**
 * 크론 표현식 변경 요청을 위한 DTO.
 */
@Data
public class CronRequest {
    /** 크론 표현식 */
    private String cronExpression;

    /**
     * 크론 표현식을 trim()하고 앞뒤 따옴표와 공백을 제거한다.
     *
     * @param cronExpression 입력된 크론 표현식
     */
    public void setCronExpression(String cronExpression) {
        if (cronExpression != null) {
            cronExpression = cronExpression.trim();
            if (cronExpression.startsWith("\"") ) {
                cronExpression = cronExpression.substring(1);
            }
            if (cronExpression.endsWith("\"") ) {
                cronExpression = cronExpression.substring(0, cronExpression.length() - 1);
            }
            cronExpression = cronExpression.trim();
        }
        this.cronExpression = cronExpression;
    }
}
