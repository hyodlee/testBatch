package egovframework.bat.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 내구성 잡의 크론 표현식 변경을 시도할 때 발생하는 예외.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DurableJobCronUpdateNotAllowedException extends RuntimeException {

    /**
     * 기본 생성자.
     */
    public DurableJobCronUpdateNotAllowedException() {
        super();
    }

    /**
     * 예외 메시지를 포함한 생성자.
     *
     * @param message 예외 메시지
     */
    public DurableJobCronUpdateNotAllowedException(String message) {
        super(message);
    }
}
