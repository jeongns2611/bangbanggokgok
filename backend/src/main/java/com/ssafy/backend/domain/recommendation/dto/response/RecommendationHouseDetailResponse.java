package com.ssafy.backend.domain.recommendation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 사용자가 선택한 단일 매물의 상세 정보를 응답하는 DTO입니다.
 * 공통 ApiResponse의 data payload로 사용됩니다.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class RecommendationHouseDetailResponse {
    private List<ImageInfo> images;
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
    private Double latitude;
    private Double longitude;
    private CommuteData commuteData;
    private InfraCount infraCount;
    private MinDist minDist;
    private DongStats dongStats;
    private Boolean isLiked;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ImageInfo {
        @JsonProperty("image_url")
        private String imageUrl;

        @JsonProperty("is_thumbnail")
        private Boolean isThumbnail;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CommuteData {
        private Integer commuteTime;
        private Double commuteDistance;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class InfraCount {
        private Integer convenienceStoreCount;
        private Integer laundryCount;
        private Integer cafeCount;
        private Integer hospitalCount;
        private Integer pharmacyCount;
        private Integer busStopCount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class MinDist {
        private Integer convenienceDist;
        private Integer laundryDist;
        private Integer cafeDist;
        private Integer hospitalDist;
        private Integer pharmacyDist;
        private Integer subwayDist;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DongStats {
        private Integer cctvCount;
        private Integer streetLightCount;
        private Integer safetyFacilityCount;
        private Double safetyScore;
        private Integer avgMeatPrice;
        private Integer avgMealPrice;
    }
}
