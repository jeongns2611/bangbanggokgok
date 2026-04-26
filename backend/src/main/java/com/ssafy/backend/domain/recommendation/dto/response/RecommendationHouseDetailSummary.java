package com.ssafy.backend.domain.recommendation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

/**
 * 매물 상세 응답의 기반이 되는 1차 조회 DTO입니다.
 * 상세 화면에 필요한 기본 필드와 후속 통계 계산에 필요한 위치/시군구 코드를 함께 담습니다.
 */
@Getter
@AllArgsConstructor
public class RecommendationHouseDetailSummary {
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
    private Integer buildYear;
    private String description;
    private Integer viewCount;
    private String floor;
    private String sigunguCode;
    private Point position;
}
