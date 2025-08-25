package egovframework.bat.erp.tasklet;

import egovframework.bat.notification.NotificationSender;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpStatus;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Mono;

/**
 * Globals.Erp.ApiUrl 프로퍼티가 주입되는지 검증하는 테스트.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FetchErpDataTaskletPropertyInjectionTest.TestConfig.class)
public class FetchErpDataTaskletPropertyInjectionTest {

    @Configuration
    @ComponentScan(basePackageClasses = FetchErpDataTasklet.class)
    static class TestConfig {

        // 테스트용 프로퍼티 설정
        @Bean
        public static PropertySourcesPlaceholderConfigurer properties() {
            Properties props = new Properties();
            props.setProperty("Globals.Erp.ApiUrl", "http://127.0.0.1:8080/api/v1/vehicles");
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

        @Bean(name = "jdbcTemplateLocal")
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

