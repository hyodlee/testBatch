package egovframework.bat.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SMS 알림 전송 컴포넌트.
 */
public class SmsNotificationSender implements NotificationSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsNotificationSender.class);

    @Override
    public void send(String message) {
        // TODO 실제 SMS 발송 구현 필요
        LOGGER.info("SMS 알림 전송: {}", message);
    }
}
