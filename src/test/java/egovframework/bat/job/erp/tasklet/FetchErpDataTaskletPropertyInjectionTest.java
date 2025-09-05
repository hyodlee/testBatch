package egovframework.bat.job.erp.tasklet;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import egovframework.bat.notification.NotificationSender;
import reactor.core.publisher.Mono;

/**
 * erp.api-url 프로퍼티가 주입되는지 검증하는 테스트.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FetchErpDataTaskletPropertyInjectionTest.TestConfig.class)
public class FetchErpDataTaskletPropertyInjectionTest {

    @Configuration
    @ComponentScan(basePackageClasses = FetchErpDataTasklet.class)
    static class TestConfig {

        // erp.api-url 프로퍼티를 직접 주입
        @Bean
        public static PropertySourcesPlaceholderConfigurer properties() {
            Properties props = new Properties();
            // 테스트에서 사용할 ERP API URL 설정
            props.setProperty("erp.api-url", "http://127.0.0.1:8080/api/v1/vehicles");
            PropertySourcesPlaceholderConfigurer config = new PropertySourcesPlaceholderConfigurer();
            config.setProperties(props);
            return config;
        }

        @Bean
        public WebClient.Builder builder() {
            return WebClient.builder().exchangeFunction(req -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("[]")
                    .build()));
        }

        // Tasklet이 기대하는 이름과 동일하게 빈을 등록
        @Bean(name = "migstgJdbcTemplate")
        public JdbcTemplate jdbcTemplate() {
            return new JdbcTemplate();
        }

        @Bean
        public List<NotificationSender> notificationSenders() {
            return Collections.emptyList();
        }
    }

    @Autowired
    FetchErpDataTasklet tasklet;

    @Test
    public void propertyInjected() throws Exception {
        // 배치 실행
        tasklet.execute(null, null);

        // apiUrl 필드에 프로퍼티가 주입되었는지 확인
        String apiUrl = tasklet.getApiUrl();
        org.junit.Assert.assertEquals("http://127.0.0.1:8080/api/v1/vehicles", apiUrl);
    }
}

