package com.ssafy.backend.global.auth.jwt;

import com.ssafy.backend.global.auth.service.AccessTokenBlacklistRedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final AccessTokenBlacklistRedisService accessTokenBlacklistRedisService;

    public JwtAuthenticationFilter(
        JwtTokenProvider jwtTokenProvider,
        AccessTokenBlacklistRedisService accessTokenBlacklistRedisService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.accessTokenBlacklistRedisService = accessTokenBlacklistRedisService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Authorization 헤더가 없으면 이 요청은 JWT 인증 대상이 아니거나,
        // 아직 로그인하지 않은 요청일 수 있으므로 그대로 다음 필터로 넘긴다.
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            String token = bearerToken.substring(BEARER_PREFIX.length());

            // 블랙리스트에 들어간 access token 은 아직 만료 전이라도 무조건 거부한다.
            if (accessTokenBlacklistRedisService.isBlacklisted(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // JWT 서명, 만료 시간, type 을 모두 통과한 access token 만 인증에 사용한다.
            if (jwtTokenProvider.validateToken(token) && "access".equals(jwtTokenProvider.getTokenType(token))) {
                SecurityContextHolder.getContext().setAuthentication(jwtTokenProvider.getAuthentication(token));
            }
        }

        filterChain.doFilter(request, response);
    }
}
