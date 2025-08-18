package egovframework.bat.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
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

    // 외부에서 전달된 프리픽스 값
    @Value("${source.system.prefix:}")
    private String prefix;

    @Override
    public EmployeeInfo process(EmployeeInfo item) {
        // 전달받은 프리픽스를 이용해 ESNTL_ID 생성
        item.setEsntlId(esntlIdGenerator.generate(prefix));
        return item;
    }
}

