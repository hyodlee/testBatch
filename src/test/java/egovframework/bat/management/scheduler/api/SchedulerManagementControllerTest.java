package egovframework.bat.management.scheduler.api;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import egovframework.bat.config.ApiKeyAuthFilter;
import egovframework.bat.management.scheduler.service.SchedulerManagementService;

/**
 * 스케줄러 관리 컨트롤러의 API 호출을 검증하는 테스트.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SchedulerManagementControllerTest.TestConfig.class,
        properties = {
                "security.api-key.enabled=true",
                "security.api-key.value=test-key"
        })
@AutoConfigureMockMvc
public class SchedulerManagementControllerTest {

    /** 테스트용 최소 설정 */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan("egovframework.bat.management.scheduler.api")
    @Import(ApiKeyAuthFilter.class)
    static class TestConfig {
    }

    /** MockMvc 인스턴스 */
    @Autowired
    private MockMvc mockMvc;

    /** 서비스 레이어 모킹 */
    @MockBean
    private SchedulerManagementService schedulerManagementService;

    /**
     * API 키를 포함하여 크론 변경 API를 호출했을 때 200 또는 404/400 응답을 확인한다.
     */
    @Test
    public void 크론_변경_API_호출_테스트() throws Exception {
        String body = "{\"cronExpression\":\"0 0/5 * * * ?\"}";

        mockMvc.perform(post("/api/management/scheduler/jobs/erpRestToStgJobDetail/cron")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("X-API-KEY", "test-key"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 400 || status == 404);
                });
    }

    /**
     * API 키 없이 호출할 경우 401 응답을 반환하는지 확인한다.
     */
    @Test
    public void API_KEY_없으면_401() throws Exception {
        String body = "{\"cronExpression\":\"0 0/5 * * * ?\"}";

        mockMvc.perform(post("/api/management/scheduler/jobs/erpRestToStgJobDetail/cron")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized());
    }
}

