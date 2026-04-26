package com.ssafy.backend.global.config;

import com.ssafy.backend.global.auth.handler.JwtAccessDeniedHandler;
import com.ssafy.backend.global.auth.handler.JwtAuthenticationEntryPoint;
import com.ssafy.backend.global.auth.jwt.JwtAuthenticationFilter;
import com.ssafy.backend.global.auth.jwt.JwtProperties;
import com.ssafy.backend.global.logging.ApiRequestLoggingFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiRequestLoggingFilter apiRequestLoggingFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(
            ApiRequestLoggingFilter apiRequestLoggingFilter,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler
    ) {
        this.apiRequestLoggingFilter = apiRequestLoggingFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // Google OAuth 인가 요청을 처리할 때는 중간 상태 저장이 필요하므로
                // 세션을 완전히 끄지 않고 "필요한 경우에만" 생성되도록 둔다.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(exception -> exception
                        // 인증 자체가 실패하면 401 응답을 내려준다.
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        // 인증은 되었지만 권한이 부족하면 403 응답을 내려준다.
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/error",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/v1/login/auth",
                                "/auth/refresh"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(apiRequestLoggingFilter, JwtAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
