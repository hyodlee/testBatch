package egovframework.bat.erp.api;

/**
 * 개발 중 ERP REST API 호출 테스트에 사용하는 더미 정보.
 * 실제 서비스와 혼동되지 않도록 더미임을 명확히 표시한다.
 */
public final class DummyErpApiInfo {
    private DummyErpApiInfo() {}

    /** 더미 ERP REST API 엔드포인트 */
    public static final String DUMMY_ENDPOINT =
        "https://dummy-erp.example.com/api/v1/customers";

    /** 더미 응답 JSON (테스트용) */
    public static final String DUMMY_RESPONSE = "{\\n"
        + "  \\\"id\\\": \\\"DUMMY-CUSTOMER-0001\\\",\\n"
        + "  \\\"name\\\": \\\"테스트 더미 고객\\\",\\n"
        + "  \\\"email\\\": \\\"dummy-customer@example.com\\\",\\n"
        + "  \\\"created_at\\\": \\\"2024-05-10T09:00:00Z\\\"\\n"
        + "}";
}
