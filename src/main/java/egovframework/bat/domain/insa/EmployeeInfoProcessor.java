package egovframework.bat.domain.insa;

import java.util.UUID;

import org.springframework.batch.item.ItemProcessor;

/**
 * ESNTL_ID 값을 세팅하는 프로세서
 */
public class EmployeeInfoProcessor implements ItemProcessor<EmployeeInfo, EmployeeInfo> {

    @Override
    public EmployeeInfo process(EmployeeInfo item) throws Exception {
        // 고유ID를 UUID로 생성
        item.setEsntlId(UUID.randomUUID().toString());
        return item;
    }
}

