package com.ssafy.backend.domain.recommendation.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecommendationRegionTopHouseRow {
    private Long houseId;
    private String regionCode;
    private String houseType;
    private String rentType;
    private Integer deposit;
    private Integer monthlyRent;
    private Double floorSize;
}
