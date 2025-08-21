package egovframework.bat.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 메일 알림 전송 컴포넌트.
 */
@Service
public class EmailNotificationSender implements NotificationSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationSender.class);

    @Override
    public void send(String message) {
        // TODO 실제 메일 발송 구현 필요
        LOGGER.info("메일 알림 전송: {}", message);
    }
}
