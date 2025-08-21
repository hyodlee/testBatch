package egovframework.bat.crm.processor;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import egovframework.bat.crm.domain.CustomerInfo;

/**
 * 고객 정보를 후처리하는 프로세서.
 * 현재는 입력 데이터를 그대로 반환한다.
 */
@Component
@StepScope
public class CustomerInfoProcessor implements ItemProcessor<CustomerInfo, CustomerInfo> {

    @Override
    public CustomerInfo process(CustomerInfo item) {
        // 필요한 추가 가공 로직은 이곳에 구현한다.
        return item;
    }
}
