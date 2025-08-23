package egovframework.bat.erp.exception;

/**
 * ERP API 호출 중 발생한 오류를 표현하는 사용자 정의 예외.
 */
public class ErpApiException extends RuntimeException {

    /** 생성자 */
    public ErpApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
