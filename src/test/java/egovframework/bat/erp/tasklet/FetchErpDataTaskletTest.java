package egovframework.bat.erp.tasklet;

import egovframework.bat.notification.NotificationSender;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

        // JdbcTemplate을 Mockito로 모의하고 batchUpdate에서 DB 접근 예외 발생을 설정
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.batchUpdate(anyString(), anyList(), anyInt(),
                any(ParameterizedPreparedStatementSetter.class)))
            .thenThrow(new DataAccessResourceFailureException("insert fail"));
        when(jdbcTemplate.update(anyString(), any(), any())).thenReturn(1);

        List<NotificationSender> senders = Collections.emptyList();
        FetchErpDataTasklet tasklet = new FetchErpDataTasklet(builder, jdbcTemplate, senders);

        // apiUrl 필드 설정
        Field field = FetchErpDataTasklet.class.getDeclaredField("apiUrl");
        field.setAccessible(true);
        field.set(tasklet, "http://example.com");

        RepeatStatus status = tasklet.execute(null, null);

        assertEquals(RepeatStatus.FINISHED, status);
        // saveDbFail 메서드 호출로 인해 erp_db_fail_log 테이블에 대한 update가 수행되었는지 검증
        verify(jdbcTemplate, atLeastOnce()).update(contains("erp_db_fail_log"), any(), any());
    }
}

