package egovframework.bat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient 설정을 담당하는 설정 클래스.
 */
@Configuration // 구성 클래스 선언
public class WebClientConfig {

    /**
     * WebClient 빌더를 빈으로 등록한다.
     *
     * @return WebClient.Builder 인스턴스
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
