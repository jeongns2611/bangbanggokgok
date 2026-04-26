package com.ssafy.backend.domain.house.dto.request;

public record UploadRequest(
        Long houseId,
        String fileName,
        String contentType,
        Boolean isThumbnail
) {
}
