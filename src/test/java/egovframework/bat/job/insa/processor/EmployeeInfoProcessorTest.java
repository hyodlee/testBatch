package egovframework.bat.job.insa.processor;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import org.junit.Test;

import egovframework.bat.job.insa.common.EsntlIdGenerator;
import egovframework.bat.job.insa.domain.EmployeeInfo;

/**
 * EmployeeInfoProcessor의 기능을 검증하는 테스트
 */
public class EmployeeInfoProcessorTest {

    @Test
    public void processGeneratesId() throws Exception {
        // 테스트용 ESNTL_ID 생성기 스텁
        EsntlIdGenerator generator = new EsntlIdGenerator(null) {
            @Override
            public String generate(String prefix) {
                return prefix + "0000001";
            }
        };
        // 프로세서 생성
        EmployeeInfoProcessor processor = new EmployeeInfoProcessor(generator);
        // sourceSystem 필드에 테스트 값 주입
        Field sourceSystemField = EmployeeInfoProcessor.class.getDeclaredField("sourceSystem");
        sourceSystemField.setAccessible(true);
        sourceSystemField.set(processor, "remote1");

        EmployeeInfo item = new EmployeeInfo();
        processor.process(item);

        // 생성된 ESNTL_ID가 예상값인지 확인
        assertEquals("LND0000001", item.getEsntlId());
    }
}
