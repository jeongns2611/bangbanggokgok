package com.ssafy.backend.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ApiRequestLogginFilter
 * <p>
 * OncePerRequestFilter를 상속 받아, 요청 1건당 한 번만 실행되는 공통 로깅 필터
 * <p>
 * 요청이 들어오면 로그를 남김
 * - method
 * - uri
 * - query
 * <p>
 * 응답이 끝나면 로그를 남김
 * - status
 * - durationMs
 * <p>
 * 예외가 발생하면 요청 응답 로그에 실패 로그도 함께 남김
 * <p>
 * 해당 로그들은 Loki에서 요청 흐름을 추적할 수 있게 하기 위해서 남김
 * <p>
 * => 어떤 API가 호출됐고, 몇 초 걸렸고, 어떤 상태로 끝났는지를 공통 포맷으로 남기는 구조
 */
@Slf4j
@Component
public class ApiRequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 관측 목적과 무관한 관리성 엔드포인트는 로그에서 제외해 노이즈를 줄인다.
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 요청 시작 시각을 기록해 응답까지 걸린 시간을 계산한다.
        long startedAt = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();

        // 모든 API 요청의 시작점을 남겨 어떤 요청이 들어왔는지 추적할 수 있게 한다.
        log.info("API request start method={} uri={} query={}", method, uri, query);

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // 필터 체인에서 예외가 전파되면 요청 문맥과 함께 에러 로그를 남긴다.
            long durationMs = System.currentTimeMillis() - startedAt;
            log.error("API request failed method={} uri={} query={} status={} durationMs={} error={}",
                    method,
                    uri,
                    query,
                    response.getStatus(),
                    durationMs,
                    e.getClass().getSimpleName(),
                    e);
            throw e;
        } finally {
            // 정상/비정상 여부와 관계없이 최종 상태 코드와 처리 시간을 항상 기록한다.
            long durationMs = System.currentTimeMillis() - startedAt;
            log.info("API request end method={} uri={} query={} status={} durationMs={}",
                    method,
                    uri,
                    query,
                    response.getStatus(),
                    durationMs);
        }
    }
}
