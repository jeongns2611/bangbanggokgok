package com.ssafy.backend.global.auth.jwt;

public record OAuthLoginResponse(
    Long userId,
    String email,
    String name,
    String profileUrl,
    String accessToken,
    String refreshToken,
    boolean isNewUser
) {
}
