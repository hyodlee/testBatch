package egovframework.bat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * 배치 인프라 설정.
 *
 * XML 기반 배치 설정을 스프링 부트 컨텍스트에 포함하기 위한 클래스이다.
 */
@Configuration
@ImportResource({
    // 배치 잡 실행기 관련 기본 설정
    "classpath:/egovframework/batch/context-batch-job-launcher.xml"
    // 개별 잡 XML은 JobRegistry를 통해 조회하므로 별도 import 불필요
})
public class BatchInfrastructureConfig {
    // 필요 시 JobLauncher 빈을 직접 정의할 수 있다.
}

