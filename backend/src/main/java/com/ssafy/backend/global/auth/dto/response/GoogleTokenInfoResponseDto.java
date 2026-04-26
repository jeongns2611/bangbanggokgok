package com.ssafy.backend.global.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenInfoResponseDto(
    String aud,
    String sub,
    String email,
    String name,
    String picture,
    @JsonProperty("email_verified")
    String emailVerified
) {
}
