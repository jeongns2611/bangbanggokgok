package com.ssafy.backend.global.auth.controller;

import com.ssafy.backend.global.auth.jwt.TokenRefreshRequest;
import com.ssafy.backend.global.auth.jwt.TokenRefreshResponse;
import com.ssafy.backend.global.auth.service.AuthService;
import com.ssafy.backend.global.common.response.ApiResponse;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.code.SuccessCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponse> refreshTokens(@Valid @RequestBody TokenRefreshRequest request) {
        // access token 이 만료된 뒤에는 refresh token 으로만 재발급을 허용한다.
        // 이 API 에서는 refresh token 이 Redis 에 저장된 최신 값인지까지 같이 확인한다.
        return ApiResponse.success(SuccessCode.SUCCESS, authService.refreshTokens(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        // Authorization 헤더는 "Bearer 실제토큰" 형태이므로, 앞의 Bearer 부분을 제거하고 순수 토큰만 꺼낸다.
        String accessToken = extractAccessToken(authorizationHeader);
        authService.logout(accessToken);
        return ApiResponse.success(SuccessCode.SUCCESS);
    }

    private String extractAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Authorization 헤더는 Bearer 토큰 형식이어야 합니다.");
        }

        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
}
