package egovframework.bat.erp.tasklet;

import egovframework.bat.notification.NotificationSender;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import static org.junit.Assert.*;

/**
 * 원격 사이트 장애 시 빈 목록을 반환할 때 정상 종료되는지 테스트.
 */
public class FetchErpDataTaskletTest {

    @Test
    public void executeFinishedWhenNoVehicles() throws Exception {
        // 원격 호출 실패를 모의하기 위한 WebClient
        WebClient.Builder builder = WebClient.builder()
            .exchangeFunction(clientRequest -> Mono.error(new RuntimeException("remote error")));

        // DB 작업이 호출되지 않도록 update를 오버라이드한 JdbcTemplate
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public int update(String sql, Object... args) {
                return 0;
            }
        };

        List<NotificationSender> senders = Collections.emptyList();
        FetchErpDataTasklet tasklet = new FetchErpDataTasklet(builder, jdbcTemplate, senders);

        // apiUrl 필드 설정
        Field field = FetchErpDataTasklet.class.getDeclaredField("apiUrl");
        field.setAccessible(true);
        field.set(tasklet, "http://example.com");

        RepeatStatus status = tasklet.execute(null, null);
        assertEquals(RepeatStatus.FINISHED, status);
    }
}

