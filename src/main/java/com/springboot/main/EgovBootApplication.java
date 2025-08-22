package com.springboot.main;

import java.util.ArrayList;
import java.util.List;

import org.egovframe.rte.bat.core.launch.support.EgovSchedulerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
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
@SpringBootApplication // 스프링 부트 애플리케이션으로 선언
@ComponentScan(basePackages = {"com.springboot.main", "egovframework.bat"}) // 스캔할 패키지 지정
public class EgovBootApplication implements CommandLineRunner {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovBootApplication.class);

	public static void main(String[] args) {
		//SpringApplication.run(EgovBootApplication.class, args);

		LOGGER.info("##### EgovSampleBootApplication Start #####");

        SpringApplication springApplication = new SpringApplication(EgovBootApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.SERVLET); // 웹 환경에서 동작하도록 설정
        springApplication.setHeadless(false);
		springApplication.setBannerMode(Banner.Mode.CONSOLE);
		springApplication.run(args);
		
		LOGGER.info("##### EgovSampleBootApplication End #####");
	}
	
	@Override
    public void run(String... args) throws Exception {
		
		List<String> jobPaths = new ArrayList<String>();

		/*
		 * 2. DB 실행 예제(DB To DB)에서 사용 할 Batch Job이 기술 된 xml파일 경로들(jobPaths)
		 */
		//example
        jobPaths.add("/egovframework/batch/job/example/mybatisToMybatisJob.xml");
        // remote1 시스템에서 STG로, 이어서 STG에서 Local로 이관하는 두 배치를 등록
        jobPaths.add("/egovframework/batch/job/insa/insaRemote1ToStgJob.xml");
        jobPaths.add("/egovframework/batch/job/insa/insaStgToLocalJob.xml");
        // ERP 시스템의 데이터를 STG와 Local로 이관하는 두 배치를 등록
        jobPaths.add("/egovframework/batch/job/erp/erpRestToStgJob.xml");
        jobPaths.add("/egovframework/batch/job/erp/erpStgToLocalJob.xml");

		/*
		 * EgovSchedulerRunner에 contextPath, schedulerJobPath, jobPaths를 인수로 넘겨서 실행한다.
		 * contextPath: Batch Job 실행에 필요한 context 정보가 기술된 xml파일 경로
		 * schedulerJobPath: Scheduler의 Trigger가 수행할 SchedulerJob(ex: QuartzJob)이 기술된 xml파일 경로
		 * jobPaths: Batch Job이 기술 된 xml 파일 경로들
		 * delayTime: Scheduler 실행을 위해 ApplicationContext를 종료를 지연시키는 시간(실행시간)
		 *            (기본 30000 milliseconds: 30초) -> Long.MAX_VALUE (약 2.9억 년(292,471,208년))
		 */
		EgovSchedulerRunner egovSchedulerRunner = new EgovSchedulerRunner("/egovframework/batch/context-batch-scheduler.xml", "/egovframework/batch/context-scheduler-job.xml",
//				jobPaths, 30000);
				jobPaths, Long.MAX_VALUE);
		egovSchedulerRunner.start();
		
    }

}
