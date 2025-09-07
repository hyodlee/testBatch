package egovframework.bat.management.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * CronRequest의 JSON 직렬화/역직렬화를 검증하는 테스트.
 */
public class CronRequestTest {

    private final ObjectMapper mapper = new ObjectMapper();

    /** 문자열 본문에서 역직렬화되는지 테스트 */
    @Test
    public void deserializeFromStringBody() throws Exception {
        CronRequest request = mapper.readValue("\"0 2 * * * ?\"", CronRequest.class);
        assertEquals("0 2 * * * ?", request.getCronExpression());
    }

    /** 객체 본문에서 역직렬화되는지 테스트 */
    @Test
    public void deserializeFromObjectBody() throws Exception {
        CronRequest request = mapper.readValue("{\"cronExpression\":\"0 2 * * * ?\"}", CronRequest.class);
        assertEquals("0 2 * * * ?", request.getCronExpression());
    }

    /** 직렬화 시 문자열 본문으로 변환되는지 테스트 */
    @Test
    public void serializeToStringBody() throws Exception {
        CronRequest request = new CronRequest("0 2 * * * ?");
        String json = mapper.writeValueAsString(request);
        assertEquals("\"0 2 * * * ?\"", json);
    }
}
