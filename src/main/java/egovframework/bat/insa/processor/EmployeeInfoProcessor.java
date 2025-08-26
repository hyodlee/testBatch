package egovframework.bat.insa.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import egovframework.bat.insa.domain.EmployeeInfo;
import egovframework.bat.insa.common.EsntlIdGenerator;
import egovframework.bat.insa.common.SourceSystemPrefix;
import lombok.RequiredArgsConstructor;

/**
 * ESNTL_ID 값을 세팅하는 프로세서
 */
@Component
@StepScope
@RequiredArgsConstructor
public class EmployeeInfoProcessor implements ItemProcessor<EmployeeInfo, EmployeeInfo> {

    /** 로깅을 위한 로거 */
    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeInfoProcessor.class);

    /** ESNTL_ID 생성기 */
    private final EsntlIdGenerator esntlIdGenerator;

    /**
     * 원천 시스템 식별 값. 잡 파라미터나 설정에서 주입된다.
     */
    @Value("#{jobParameters['sourceSystem']}")
    private String sourceSystem;

    @Override
    public EmployeeInfo process(EmployeeInfo item) throws Exception {

        // 원천 시스템에 해당하는 프리픽스를 조회
        String prefix = SourceSystemPrefix.getPrefix(sourceSystem);
        // 프리픽스가 비어있으면 기본값 LND 사용
        if (prefix.isEmpty()) {
            prefix = "LND";
        }
        // 프리픽스로 ESNTL_ID 생성
        item.setEsntlId(esntlIdGenerator.generate(prefix));
        // 디버그 로그: 원천 시스템과 프리픽스, 생성된 ID를 출력
        LOGGER.debug("sourceSystem={}, prefix={}, generatedId={}", sourceSystem, prefix, item.getEsntlId());

        return item;
    }
}

