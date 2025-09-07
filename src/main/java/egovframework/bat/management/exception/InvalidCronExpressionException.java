package egovframework.bat.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 잘못된 크론 표현식 사용 시 발생하는 예외.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCronExpressionException extends RuntimeException {

    /**
     * 기본 생성자.
     */
    public InvalidCronExpressionException() {
        super();
    }

    /**
     * 예외 메시지를 포함한 생성자.
     *
     * @param message 예외 메시지
     */
    public InvalidCronExpressionException(String message) {
        super(message);
    }
}
