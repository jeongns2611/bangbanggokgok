package com.ssafy.backend.domain.user.dto.response;

import java.time.LocalDateTime;

public record UserNeedUpdateResponseDto(
        Long id,
        LocalDateTime updatedAt,
        UserNeedDetailResponseDto userNeed
) {
}
