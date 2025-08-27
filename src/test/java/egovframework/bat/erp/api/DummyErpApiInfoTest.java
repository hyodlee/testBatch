package egovframework.bat.erp.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * 더미 ERP API 정보와 응답 JSON 파일을 검증하는 테스트.
 */
public class DummyErpApiInfoTest {

    /**
     * JSON 파일에서 더미 응답을 읽어 올 수 있는지 확인한다.
     */
    @Test
    public void 더미_JSON_파일_읽기_테스트() throws Exception {
        URL resource = getClass().getClassLoader().getResource("dummy-vehicle-response.json");
        String json = new String(
                Files.readAllBytes(Paths.get(resource.toURI())),
                StandardCharsets.UTF_8);
        assertTrue(json.contains("\"vehicleId\": \"DUMMY-VEHICLE-0001\""));
    }

    /**
     * 더미 ERP 엔드포인트 상수가 예상 값과 일치하는지 확인한다.
     */
    @Test
    public void 더미_엔드포인트_상수_테스트() {
        assertEquals("https://dummy-erp.example.com/api/v1/vehicles", DummyErpApiInfo.DUMMY_ENDPOINT);
    }
}
