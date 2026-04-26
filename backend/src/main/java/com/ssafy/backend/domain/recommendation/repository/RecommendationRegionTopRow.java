package com.ssafy.backend.domain.recommendation.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 지역구 추천 집계용 Row DTO
 */
@Getter
@AllArgsConstructor
public class RecommendationRegionTopRow {
    private String regionCode;
    private String sigunguName;
    private Integer convenienceStoreCount;
    private Integer cafeCount;
    private Integer hospitalCount;
    private Integer selectedRegionFoodCost;
}

