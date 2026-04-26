package com.ssafy.backend.domain.statistics.service;

import com.ssafy.backend.domain.house.dto.response.HouseCompareResponse;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationHouseDetailResponse;
import com.ssafy.backend.domain.statistics.dto.response.RegionInfraStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DongStatsComposer {

    private final RegionSafetyProvider regionSafetyProvider;
    private final SeoulFoodCostProvider seoulFoodCostProvider;

    public RecommendationHouseDetailResponse.DongStats composeForRecommendation(String sigunguCode) {
        RegionInfraStatisticsResponse.RegionSafety regionSafety = regionSafetyProvider.getRegionSafety(sigunguCode);
        SeoulFoodCostProvider.FoodCost foodCost = seoulFoodCostProvider.getFoodCost(sigunguCode);

        return RecommendationHouseDetailResponse.DongStats.builder()
                .cctvCount(defaultZero(regionSafety.getCctvCount()))
                .streetLightCount(defaultZero(regionSafety.getStreetLightCount()))
                .safetyFacilityCount(defaultZero(regionSafety.getSecurityFacilityCount()))
                .safetyScore(defaultZero(regionSafety.getCompositeSafetyScore()))
                .avgMeatPrice(defaultZero(foodCost.avgMeatPrice()))
                .avgMealPrice(defaultZero(foodCost.avgMealPrice()))
                .build();
    }

    public HouseCompareResponse.DongStats composeForCompare(String sigunguCode) {
        RegionInfraStatisticsResponse.RegionSafety regionSafety = regionSafetyProvider.getRegionSafety(sigunguCode);
        SeoulFoodCostProvider.FoodCost foodCost = seoulFoodCostProvider.getFoodCost(sigunguCode);

        return HouseCompareResponse.DongStats.builder()
                .cctvCount(defaultZero(regionSafety.getCctvCount()))
                .streetLightCount(defaultZero(regionSafety.getStreetLightCount()))
                .safetyFacilityCount(defaultZero(regionSafety.getSecurityFacilityCount()))
                .safetyScore(defaultZero(regionSafety.getCompositeSafetyScore()))
                .avgMeatPrice(defaultZero(foodCost.avgMeatPrice()))
                .avgMealPrice(defaultZero(foodCost.avgMealPrice()))
                .build();
    }

    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Double defaultZero(Double value) {
        return value == null ? 0.0 : value;
    }
}
