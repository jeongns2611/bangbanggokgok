package com.ssafy.backend.global.auth.jwt;

public record TokenRefreshResponse(
    String accessToken,
    String refreshToken
) {
}
