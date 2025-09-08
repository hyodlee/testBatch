package egovframework.bat.management.scheduler.exception;

/**
 * 내구성 잡의 일시 중지 또는 재개를 시도할 때 발생하는 예외.
 */
public class DurableJobPauseResumeNotAllowedException extends RuntimeException {

    /**
     * 기본 생성자.
     */
    public DurableJobPauseResumeNotAllowedException() {
        super();
    }

    /**
     * 예외 메시지를 포함한 생성자.
     *
     * @param message 예외 메시지
     */
    public DurableJobPauseResumeNotAllowedException(String message) {
        super(message);
    }
}
