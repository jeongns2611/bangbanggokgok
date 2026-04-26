package com.ssafy.backend.global.auth.dto.response;

public record LoginAuthResponse(
    Long userId,
    String googleEmail,
    String name,
    String profileUrl,
    String accessToken,
    String refreshToken,
    boolean isNewUser
) {
}
