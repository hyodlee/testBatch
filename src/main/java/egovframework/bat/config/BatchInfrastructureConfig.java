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
    "classpath:/egovframework/batch/context-batch-job-launcher.xml",
    // ERP REST -> STG 배치 잡 설정을 로딩하여 Job 빈을 등록
    "classpath:/egovframework/batch/job/erp/erpRestToStgJob.xml"
})
public class BatchInfrastructureConfig {
    // 필요 시 JobLauncher 빈을 직접 정의할 수 있다.
}

