package egovframework.bat.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리용 API 호출 시 API 키를 검증하는 필터
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "security.api-key", name = "enabled", havingValue = "true")
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    /** 기대하는 API 키 값 */
    @Value("${security.api-key.value}")
    private String expectedKey;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        // 관리용 API 경로인지 확인
        String uri = req.getRequestURI();
        if (!uri.startsWith("/api/management")) {
            chain.doFilter(req, res);
            return;
        }

        // 헤더에서 API 키 추출
        String key = req.getHeader("X-API-KEY");
        if (expectedKey.equals(key)) {
            chain.doFilter(req, res);   // 정상 흐름
        } else {
            // API 키가 없거나 일치하지 않는 경우 경고 로그 출력
            log.warn("API 키 불일치 - URI: {}", uri);
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }
}
