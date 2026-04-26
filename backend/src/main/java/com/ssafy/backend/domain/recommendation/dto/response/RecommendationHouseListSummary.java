package com.ssafy.backend.domain.recommendation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 메인 매물 조회 결과를 평탄화해 담는 내부 DTO입니다.
 * house_image를 함께 조인하면 매물 1건이 이미지 수만큼 반복되므로,
 * 메인 매물 정보만 먼저 조회한 뒤 서비스 계층에서 이미지 목록을 합칩니다.
 */
@Getter
@AllArgsConstructor
public class RecommendationHouseListSummary {
    private Long houseId;
    private String dong;
    private String houseType;
    private String rentType;
    private String houseStatus;
    private Integer deposit;
    private Integer monthlyCost;
    private Integer managementCost;
    private String managementItems;
    private Double floorSize;
    private String floor;
}
