/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package egovframework.bat.scheduler;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;

import egovframework.bat.management.JobProgressService;
import egovframework.bat.service.JobLockService;

/**
 * Quartz 스케줄러에서 Spring Batch Job을 실행하는 클래스입니다.
 *
 * @author 배치실행개발팀
 * @since 2012. 07.25
 * @version 1.0
 * @see
 *  <pre>
 *      개정이력(Modification Information)
 *
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012. 07.25  배치실행개발팀     최초 생성
 *  </pre>
 */

public class EgovQuartzJobLauncher extends QuartzJobBean {

        /** JobDataMap에서 실행할 잡의 이름을 나타내는 키 */
        static final String JOB_NAME = "jobName";

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovQuartzJobLauncher.class);

        /** 실행할 배치 잡 */
        private Job job;

        private JobLauncher jobLauncher;

        /** 중복 실행을 방지하기 위한 락 서비스 */
        private JobLockService jobLockService;

        /** 진행 상황 전송을 위한 서비스 */
        private JobProgressService jobProgressService;

        /**
         * Public setter for the {@link JobLauncher}.
         * @param jobLauncher the {@link JobLauncher} to set
         */
        public void setJobLauncher(JobLauncher jobLauncher) {
                this.jobLauncher = jobLauncher;
        }

        /**
         * 실행할 {@link Job} 주입.
         * @param job 실행할 배치 잡
         */
        public void setJob(Job job) {
                this.job = job;
        }

        /**
         * {@link JobLockService} 주입.
         * @param jobLockService 락 서비스
         */
        public void setJobLockService(JobLockService jobLockService) {
                this.jobLockService = jobLockService;
        }

        /**
         * {@link JobProgressService} 주입.
         * @param jobProgressService 진행 상황 서비스
         */
        public void setJobProgressService(JobProgressService jobProgressService) {
                this.jobProgressService = jobProgressService;
        }

	@Override
	@SuppressWarnings("unchecked")
	protected void executeInternal(JobExecutionContext context) {
		Long timestamp = null;
		Map<String, Object> jobDataMap = context.getMergedJobDataMap();
		//LOGGER.debug("JobDataMap: {}", jobDataMap); // JobDataMap 디버그 로그
		LOGGER.info("JobDataMap: {}", jobDataMap); // JobDataMap 디버그 로그
                String jobName = (String) jobDataMap.get(JOB_NAME);

		/*
		 * 주기적으로 실행가능하도록 하기 위해, JobParamter의 timestamp 값을 갱신한다.
		 */
		if (jobDataMap.containsKey("timestamp")) {
                        jobDataMap.remove("timestamp");
		}
		timestamp = new Date().getTime();
		jobDataMap.put("timestamp", timestamp);
		LOGGER.debug("timestamp: {}", timestamp); // timestamp 디버그 로그

                LOGGER.warn("Quartz trigger firing with Spring Batch jobName={}", jobName);

        if (!jobLockService.tryLock(jobName)) {
            LOGGER.warn("{} 작업이 이미 실행 중이므로 스케줄러 실행을 건너뜀", jobName);
            return;
        }

        JobParameters jobParameters = getJobParametersFromJobMap(jobDataMap);
        LOGGER.info("EgovQuartzJobLauncher.executeInternal(): JobParameters: {}", jobParameters); // JobParameters 디버그 로그
        jobProgressService.send(jobName, "STARTED");
        try {
            LOGGER.info("{} 작업 시작", jobName); // 작업 시작 로그
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);
            LOGGER.info("{} 작업 종료, 상태: {}", jobName, jobExecution.getStatus()); // 작업 종료 로그
            jobProgressService.send(jobName, jobExecution.getStatus().toString());
        } catch (JobExecutionException e) {
            LOGGER.error("Could not execute job.", e);
            jobProgressService.send(jobName, "FAILED");
        } finally {
            jobLockService.unlock(jobName);
        }
    }

	/*
	 * Copy parameters that are of the correct type over to
	 * {@link JobParameters}, ignoring jobName.
	 *
	 * @return a {@link JobParameters} instance
	 */
	private JobParameters getJobParametersFromJobMap(Map<String, Object> jobDataMap) {
		JobParametersBuilder builder = new JobParametersBuilder();

		for (Entry<String, Object> entry : jobDataMap.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof String && !key.equals(JOB_NAME)) {
				builder.addString(key, (String) value);
			} else if (value instanceof Float || value instanceof Double) {
				builder.addDouble(key, ((Number) value).doubleValue());
			} else if (value instanceof Integer || value instanceof Long) {
				builder.addLong(key, ((Number) value).longValue());
			} else if (value instanceof Date) {
				builder.addDate(key, (Date) value);
			} else {
				//LOGGER.debug("JobDataMap contains values which are not job parameters (ignoring).");
				LOGGER.info("JobDataMap contains values which are not job parameters (ignoring).");
			}
		}
		return builder.toJobParameters();
	}
}
