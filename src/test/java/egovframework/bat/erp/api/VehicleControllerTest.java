package egovframework.bat.erp.api;

import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * VehicleController의 동작을 검증하는 테스트.
 */
public class VehicleControllerTest {

    /**
     * 차량 목록 조회 API가 정상적으로 응답하는지 확인한다.
     */
    @Test
    public void 차량_목록_조회_API_테스트() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new VehicleController()).build();

        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vehicleId").value("SAMPLE-0001"));
    }
}

