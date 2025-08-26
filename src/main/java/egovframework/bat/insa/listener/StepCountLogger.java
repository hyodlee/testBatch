package egovframework.bat.insa.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * 배치 스텝 실행 후 처리 건수(read/write/skip)를 기록하는 리스너.
 */
@Component
public class StepCountLogger implements StepExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(StepCountLogger.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        // 사전 처리 없음
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        // 스텝 처리 건수를 디버그 로그로 출력
        LOGGER.debug("readCount={}, writeCount={}, skipCount={}",
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount());
        return stepExecution.getExitStatus();
    }
}
