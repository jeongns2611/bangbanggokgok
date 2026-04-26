package com.ssafy.backend.domain.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
/**
 * 지역 생활 통계 통합 응답 DTO
 */
public class RegionInfraStatisticsResponse {

    private Region region;
    private LivingCostIndex livingCostIndex;
    private LivingInfrastructure livingInfrastructure;
    private RegionSafety regionSafety;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Region {
        private String sidoName;
        private String sigunguName;
        private String sigunguCode;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LivingCostIndex {
        private CostIndex porkBellyIndex;
        private CostIndex kimbapIndex;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CostIndex {
        // 최근 12개월 평균 금액(천원 단위)
        private Integer avgLast12MonthsThousandWon;
        // 최근 12개월 월별 평균 금액 목록(천원 단위)
        private List<MonthlyAveragePrice> monthlyAveragePrices;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    // 월별 평균 금액
    public static class MonthlyAveragePrice {
        // 기준 연월(YYYYMM)
        private Integer baseYearMonth;
        // 평균 금액(천원 단위)
        private Integer averagePriceThousandWon;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    // 생활 인프라 집계
    public static class LivingInfrastructure {
        private Integer convenienceStoreCount;
        private Integer cafeCount;
        private Integer hospitalCount;
        private Integer laundryCount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    // 지역 안전도
    public static class RegionSafety {
        private Integer cctvCount;
        private Integer streetLightCount;
        private Integer securityFacilityCount;
        private Double compositeSafetyScore;
    }
}
