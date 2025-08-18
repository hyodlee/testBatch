package egovframework.bat.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import egovframework.bat.domain.insa.EmployeeInfo;
import egovframework.bat.domain.insa.EsntlIdGenerator;
import egovframework.bat.domain.insa.SourceSystemPrefix;
import lombok.RequiredArgsConstructor;

/**
 * 직원 정보에 고유ID와 소스시스템 값을 설정하는 Processor
 */
@Component
@RequiredArgsConstructor
public class EmployeePostProcessor implements ItemProcessor<EmployeeInfo, EmployeeInfo> {

    private final EsntlIdGenerator esntlIdGenerator;
    private static final String SOURCE_SYSTEM = "LOCAL";

    @Override
    public EmployeeInfo process(EmployeeInfo item) {
        String prefix = SourceSystemPrefix.getPrefix(item.getSourceSystem());
        item.setEsntlId(esntlIdGenerator.generate(prefix));
        item.setSourceSystem(SOURCE_SYSTEM);
        return item;
    }
}

