package com.ssafy.backend.global.auth.jwt;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
    @NotBlank(message = "refreshToken 은 필수입니다.")
    String refreshToken
) {
}
