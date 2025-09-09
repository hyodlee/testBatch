package egovframework.bat.management.scheduler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 특정 잡의 트리거를 찾을 수 없을 때 발생하는 예외.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TriggerNotFoundException extends RuntimeException {

    /**
     * 기본 생성자.
     */
    public TriggerNotFoundException() {
        super();
    }

    /**
     * 예외 메시지를 포함한 생성자.
     *
     * @param message 예외 메시지
     */
    public TriggerNotFoundException(String message) {
        super(message);
    }
}
