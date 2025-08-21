package egovframework.bat.notification;

/**
 * 장애 발생 시 알림을 전송하기 위한 인터페이스.
 */
public interface NotificationSender {

    /**
     * 알림 메시지를 전송한다.
     *
     * @param message 전송할 메시지
     */
    void send(String message);
}
