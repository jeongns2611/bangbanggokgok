package com.ssafy.backend.domain.house.dto.request;

public record CompleteRequest(
        Long houseId,
        String objectKey,
        Boolean isThumbnail
) {
}
