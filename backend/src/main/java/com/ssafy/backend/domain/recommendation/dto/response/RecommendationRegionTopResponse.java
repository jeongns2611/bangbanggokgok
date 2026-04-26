package com.ssafy.backend.domain.recommendation.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지역구 TOP3 추천 및 통계 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRegionTopResponse {

    private AppliedFilter appliedFilter;

    @Builder.Default
    private List<RecommendationItem> recommendations = List.of();

    private Meta meta;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppliedFilter {
        private String sidoName;
        private CodeInfo houseType;
        private CodeInfo rentType;
        private String priceRangePolicy;
        private String areaRangePolicy;
        private String houseTypePolicy;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CodeInfo {
        private Long codeId;
        private String codeName;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        // 전체 시군구 정렬 기준의 순위
        private Integer rank;
        // TOP3 포함 여부와 TOP3 내 순위(1~3)
        private Boolean top3;
        private Integer top3Rank;
        private String sigunguName;
        private String regionCode;
        private CodeInfo rentType;
        private Boolean hasMatchingHouses;
        private Integer matchingHouseCount;
        private HousingCost housingCost;
        private FacilityCount facilityCount;
        private Commute commute;
        private LivingCost livingCost;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HousingCost {
        private Integer avgDeposit;
        private Integer avgMonthlyRent;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacilityCount {
        private Integer convenienceStoreCount;
        private Integer cafeCount;
        private Integer hospitalCount;
        private Integer totalCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Commute {
        private Integer avgCommuteMinutes;
        private Double avgCommuteDistanceKm;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LivingCost {
        private Integer foodCostIndex;
        private Integer selectedRegionFoodCost;
        private Integer seoulAvgFoodCost;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private Integer topN;
        private Integer totalDistrictCount;
        private String baseCodePolicy;
        private Boolean excludedInactiveCodes;
        private String sortPolicy;
    }
}
