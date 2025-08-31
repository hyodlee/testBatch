package egovframework.bat.config;

import egovframework.bat.notification.EmailNotificationSender;
import egovframework.bat.notification.NotificationSender;
import egovframework.bat.notification.SmsNotificationSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 공통 알림 컴포넌트를 빈으로 등록하는 설정 클래스.
 */
@Configuration
public class NotificationConfig {

    /**
     * 이메일 알림 전송기를 빈으로 등록한다.
     *
     * @return EmailNotificationSender 인스턴스
     */
    @Bean
    public NotificationSender emailNotificationSender() {
        return new EmailNotificationSender();
    }

    /**
     * SMS 알림 전송기를 빈으로 등록한다.
     *
     * @return SmsNotificationSender 인스턴스
     */
    @Bean
    public NotificationSender smsNotificationSender() {
        return new SmsNotificationSender();
    }
}
