package egovframework.bat.insa.util;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 소스 시스템별 ESNTL_ID 프리픽스를 관리하는 열거형.
 */
@Getter
@RequiredArgsConstructor
public enum SourceSystemPrefix {

    REMOTE1("remote1", "LND"),
    REMOTE2("remote2", "ETC");

    private final String system;
    private final String prefix;

    /**
     * 소스 시스템에 해당하는 프리픽스를 반환한다.
     *
     * @param system 소스 시스템 값
     * @return 매핑된 프리픽스, 없으면 "LND"
     */
    public static String getPrefix(String system) {
        if (system == null) {
            return "LND";
        }
        return Arrays.stream(values())
            .filter(v -> v.system.equalsIgnoreCase(system))
            .map(SourceSystemPrefix::getPrefix)
            .findFirst()
            .orElse("LND");
    }
}
