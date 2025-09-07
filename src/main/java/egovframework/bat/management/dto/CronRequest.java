package egovframework.bat.management.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 크론 표현식 변경 요청을 위한 DTO.
 */
@Data
@NoArgsConstructor
public class CronRequest {
    /** 크론 표현식 */
    private String cronExpression;

    /**
     * 문자열 본문 역직렬화를 위한 생성자.
     *
     * @param cronExpression 크론 표현식
     */
    @JsonCreator
    public CronRequest(String cronExpression) {
        setCronExpression(cronExpression);
    }

    /**
     * JSON 직렬화를 위한 Getter.
     *
     * @return 크론 표현식
     */
    @JsonValue
    public String getCronExpression() {
        return cronExpression;
    }

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
