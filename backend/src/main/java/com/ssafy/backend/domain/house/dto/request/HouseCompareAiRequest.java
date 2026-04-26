package com.ssafy.backend.domain.house.dto.request;

import java.util.List;

public record HouseCompareAiRequest(
        List<HouseItem> comparisonData
) {

    public record HouseItem(
            Long houseId,
            String dong,
            String houseType,
            String rentType,
            Integer deposit,
            Integer monthlyCost,
            Double floorSize,
            String floor,
            String description,
            CommuteData commuteData,
            InfraCount infraCount,
            MinDist minDist,
            DongStats dongStats,
            ScoreSummary scoreSummary
    ) {
    }

    public record CommuteData(
            Integer commuteTime,
            Double commuteDistance
    ) {
    }

    public record InfraCount(
            Integer convenienceStoreCount,
            Integer laundryCount,
            Integer cafeCount,
            Integer hospitalCount,
            Integer pharmacyCount,
            Integer busStopCount
    ) {
    }

    public record MinDist(
            Integer convenienceDist,
            Integer laundryDist,
            Integer cafeDist,
            Integer hospitalDist,
            Integer pharmacyDist,
            Integer subwayDist
    ) {
    }

    public record DongStats(
            Integer cctvCount,
            Integer streetLightCount,
            Integer safetyFacilityCount,
            Double safetyScore,
            Integer avgMeatPrice,
            Integer avgMealPrice
    ) {
    }

    public record ScoreSummary(
            Integer trafficScore,
            Integer infraScore,
            Integer safetyScore,
            Integer needFitScore
    ) {
    }
}
