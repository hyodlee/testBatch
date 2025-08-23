package egovframework.bat.erp.tasklet;

import egovframework.bat.notification.NotificationSender;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpStatus;
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

    @Test
    public void executeFinishedWhenInsertFails() throws Exception {
        // 정상 데이터를 반환하는 WebClient 구성
        String json = "[{\"vehicleId\":\"1\",\"model\":\"model\",\"manufacturer\":\"maker\",\"price\":100}]";
        WebClient.Builder builder = WebClient.builder()
            .exchangeFunction(clientRequest -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(json)
                    .build()
            ));

        // insert 시 예외를 발생시키고 update 호출 횟수를 기록하는 JdbcTemplate
        class TestJdbcTemplate extends JdbcTemplate {
            int updateCount = 0;

            @Override
            public <T> int[] batchUpdate(String sql, java.util.List<T> batchArgs, int batchSize,
                                        org.springframework.jdbc.core.ParameterizedPreparedStatementSetter<T> pss) {
                throw new RuntimeException("insert fail");
            }

            @Override
            public int update(String sql, Object... args) {
                updateCount++;
                return 1;
            }
        }

        TestJdbcTemplate jdbcTemplate = new TestJdbcTemplate();
        List<NotificationSender> senders = Collections.emptyList();
        FetchErpDataTasklet tasklet = new FetchErpDataTasklet(builder, jdbcTemplate, senders);

        // apiUrl 필드 설정
        Field field = FetchErpDataTasklet.class.getDeclaredField("apiUrl");
        field.setAccessible(true);
        field.set(tasklet, "http://example.com");

        RepeatStatus status = tasklet.execute(null, null);

        assertEquals(RepeatStatus.FINISHED, status);
        assertTrue("로그 저장이 호출되어야 합니다.", jdbcTemplate.updateCount > 0);
    }
}

