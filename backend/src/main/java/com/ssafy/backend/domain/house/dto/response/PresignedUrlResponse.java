package com.ssafy.backend.domain.house.dto.response;

public record PresignedUrlResponse(
        Long houseId,
        String url,
        String objectKey,
        Boolean isThumbnail
) {
}
