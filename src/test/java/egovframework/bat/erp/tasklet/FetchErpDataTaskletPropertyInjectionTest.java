package egovframework.bat.erp.tasklet;

import egovframework.bat.notification.NotificationSender;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

/**
 * application.yml의 erp.api-url 프로퍼티가 주입되는지 검증하는 테스트.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FetchErpDataTaskletPropertyInjectionTest.TestConfig.class)
public class FetchErpDataTaskletPropertyInjectionTest {

    @Configuration
    @ComponentScan(basePackageClasses = FetchErpDataTasklet.class)
    static class TestConfig {

        // Yaml 설정을 읽어 PropertySourcesPlaceholderConfigurer에 등록
        @Bean
        public static PropertySourcesPlaceholderConfigurer properties() {
            YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
            yaml.setResources(new ClassPathResource("application.yml"));
            PropertySourcesPlaceholderConfigurer config = new PropertySourcesPlaceholderConfigurer();
            config.setProperties(yaml.getObject());
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

        @Bean
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
        Field field = FetchErpDataTasklet.class.getDeclaredField("apiUrl");
        field.setAccessible(true);
        String apiUrl = (String) field.get(tasklet);
        org.junit.Assert.assertEquals("http://127.0.0.1:8080/api/v1/vehicles", apiUrl);
    }
}

