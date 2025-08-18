package egovframework.bat.domain.insa;

import java.util.UUID;

import org.springframework.batch.item.ItemProcessor;

/**
 * ESNTL_ID와 SOURCE_SYSTEM 값을 세팅하는 프로세서
 */
public class EmployeeInfoProcessor implements ItemProcessor<EmployeeInfo, EmployeeInfo> {

    @Override
    public EmployeeInfo process(EmployeeInfo item) throws Exception {
        // 고유ID를 UUID로 생성
        item.setEsntlId(UUID.randomUUID().toString());
        // 원천시스템 값을 STG로 설정
        item.setSourceSystem("STG");
        return item;
    }
}

