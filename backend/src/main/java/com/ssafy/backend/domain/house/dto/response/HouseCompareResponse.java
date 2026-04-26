package com.ssafy.backend.domain.house.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

public record HouseCompareResponse(
        List<ComparisonData> comparisonData
) {

    public record ComparisonData(
            List<CompareImage> images,
            Long houseId,
            String dong,
            String houseType,
            String rentType,
            String houseStatus,
            Integer deposit,
            Integer monthlyCost,
            Integer managementCost,
            String managementItems,
            Double floorSize,
            Integer buildYear,
            String description,
            Integer viewCount,
            String floor,
            List<CommuteData> commuteData,
            InfraCount infraCount,
            MinDist minDist,
            DongStats dongStats,
            ScoreSummary scoreSummary
    ) {
    }

    public record ScoreSummary(
            Integer trafficScore,
            Integer infraScore,
            Integer safetyScore,
            Integer needFitScore
    ) {
    }

    public record CompareImage(
            @JsonProperty("image_url")
            String imageUrl,
            @JsonProperty("is_thumbnail")
            Boolean isThumbnail
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

    @Builder
    public record DongStats(
            Integer cctvCount,
            Integer streetLightCount,
            Integer safetyFacilityCount,
            Double safetyScore,
            Integer avgMeatPrice,
            Integer avgMealPrice
    ) {
    }
}
