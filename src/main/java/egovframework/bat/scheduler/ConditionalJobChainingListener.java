package egovframework.bat.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobChainingJobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

/**
 * 배치 잡의 종료 상태를 확인해 성공한 경우에만 후행 Quartz 잡을 기동하는 리스너.
 */
public class ConditionalJobChainingListener extends JobChainingJobListener implements JobExecutionListener {

    /** 로그 기록용 로거 */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionalJobChainingListener.class);

    /** 잡별 종료 상태 저장소 (JobDetail 이름 기준) */
    private final Map<String, ExitStatus> jobExitStatuses = new ConcurrentHashMap<>();

    public ConditionalJobChainingListener(String name) {
        super(name);
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // 별도의 전처리가 필요하지 않으므로 구현하지 않음
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobDetailName = jobExecution.getJobInstance().getJobName() + "Detail";
        ExitStatus exitStatus = jobExecution.getExitStatus();
        jobExitStatuses.put(jobDetailName, exitStatus);
        LOGGER.info("잡 '{}' 종료 상태 기록: {}", jobDetailName, exitStatus);
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        String jobDetailName = context.getJobDetail().getKey().getName();
        ExitStatus exitStatus = jobExitStatuses.get(jobDetailName);

        if (ExitStatus.COMPLETED.equals(exitStatus)) {
            LOGGER.info("잡 '{}'이(가) 성공적으로 완료되어 후행 잡을 기동합니다.", jobDetailName);
            super.jobWasExecuted(context, jobException);
        } else {
            if (exitStatus == null) {
                LOGGER.warn("잡 '{}'의 종료 상태 정보를 찾을 수 없어 후행 잡을 기동하지 않습니다.", jobDetailName);
            } else {
                LOGGER.warn("잡 '{}'의 종료 상태가 '{}'이므로 후행 잡을 기동하지 않습니다.", jobDetailName, exitStatus);
            }
        }

        if (jobDetailName != null) {
            jobExitStatuses.remove(jobDetailName);
        }
    }
}
