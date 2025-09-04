package com.springboot.main;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author 배치실행개발팀
 * @since 2021. 11.25
 * @version 1.0
 * @see
 *  <pre>
 *      개정이력(Modification Information)
 *
 *  수정일               수정자                 수정내용
 *  ----------   -----------   ---------------------------
 *  2021.11.25   신용호                 최초 생성
 *  
 *  </pre>
*/

@SpringBootApplication
@ComponentScan(basePackages = {"com.springboot.main", "egovframework.bat"})
// 서비스와 리포지토리 매퍼가 STG 데이터소스를 사용하도록 SqlSessionFactory 지정
@MapperScan(basePackages = {"egovframework.bat.service", "egovframework.bat.repository"}, sqlSessionFactoryRef = "sqlSessionFactory-stg")
public class EgovBootApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovBootApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(EgovBootApplication.class, args);

//		LOGGER.info("##### EgovSampleBootApplication Start #####");
//
//        SpringApplication springApplication = new SpringApplication(EgovBootApplication.class);
//        springApplication.setWebApplicationType(WebApplicationType.SERVLET); // 웹 환경에서 동작하도록 설정
//        springApplication.setHeadless(false);
//		springApplication.setBannerMode(Banner.Mode.CONSOLE);
//		springApplication.run(args);
//		
//		LOGGER.info("##### EgovSampleBootApplication End #####");
	}
	
    @Override
    public void run(String... args) throws Exception {
        // 자바 기반 배치 설정을 사용하므로 추가 실행 코드는 필요하지 않습니다.
    }

}
