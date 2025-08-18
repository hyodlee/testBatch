package egovframework.bat.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import egovframework.bat.domain.insa.EmployeeInfo;
import egovframework.bat.domain.insa.EsntlIdGenerator;
import lombok.RequiredArgsConstructor;

/**
 * 직원 정보에 고유ID를 설정하는 Processor
 */
@Component
@RequiredArgsConstructor
public class EmployeePostProcessor implements ItemProcessor<EmployeeInfo, EmployeeInfo> {

    private final EsntlIdGenerator esntlIdGenerator;

    @Override
    public EmployeeInfo process(EmployeeInfo item) {
        // ESNTL_ID를 생성하여 설정
        item.setEsntlId(esntlIdGenerator.generate(""));
        return item;
    }
}

