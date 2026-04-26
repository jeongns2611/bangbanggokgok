package com.ssafy.backend.domain.recommendation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 이미지 목록 조립용 내부 DTO입니다.
 */
@Getter
@AllArgsConstructor
public class RecommendationHouseImageRow {
    private Long houseId;
    private String imageUrl;
    private Boolean isThumbnail;
}
