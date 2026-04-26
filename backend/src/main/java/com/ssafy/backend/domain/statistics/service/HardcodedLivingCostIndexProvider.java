package com.ssafy.backend.domain.statistics.service;

import com.ssafy.backend.domain.statistics.dto.response.RegionInfraStatisticsResponse;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
/**
 * TODO: 추후 변경 필요
 * 생활비 지수 하드코딩 Provider
 * 실제 데이터 소스(통계 DB/외부 API) 연동 전까지 임시로 사용
 */
public class HardcodedLivingCostIndexProvider implements LivingCostIndexProvider {

    private static final int[] DEFAULT_PORK_BELLY = {17, 17, 18, 18, 19, 19, 18, 18, 17, 18, 18, 19};
    private static final int[] DEFAULT_KIMBAP = {3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4};

    private static final Map<String, int[]> PORK_BELLY_BY_SIGUNGU = Map.of(
            "11680", new int[]{18, 18, 19, 19, 20, 20, 19, 19, 18, 19, 19, 20}
    );
    private static final Map<String, int[]> KIMBAP_BY_SIGUNGU = Map.of(
            "11680", new int[]{4, 4, 4, 4, 5, 5, 4, 4, 4, 4, 5, 5}
    );

    @Override
    public RegionInfraStatisticsResponse.LivingCostIndex getLivingCostIndex(String sigunguCode) {
        // 시군구별 샘플 데이터가 없으면 기본 샘플 데이터를 사용
        int[] porkBellySeries = PORK_BELLY_BY_SIGUNGU.getOrDefault(sigunguCode, DEFAULT_PORK_BELLY);
        int[] kimbapSeries = KIMBAP_BY_SIGUNGU.getOrDefault(sigunguCode, DEFAULT_KIMBAP);

        return RegionInfraStatisticsResponse.LivingCostIndex.builder()
                .porkBellyIndex(buildCostIndex(porkBellySeries))
                .kimbapIndex(buildCostIndex(kimbapSeries))
                .build();
    }

    private RegionInfraStatisticsResponse.CostIndex buildCostIndex(int[] monthlyValues) {
        List<RegionInfraStatisticsResponse.MonthlyAveragePrice> monthlyTrend = buildMonthlyTrend(monthlyValues);
        // 12개월 평균(천원 단위 정수) 계산
        int average = (int) Math.round(Arrays.stream(monthlyValues).average().orElse(0));

        return RegionInfraStatisticsResponse.CostIndex.builder()
                .avgLast12MonthsThousandWon(average)
                .monthlyAveragePrices(monthlyTrend)
                .build();
    }

    private List<RegionInfraStatisticsResponse.MonthlyAveragePrice> buildMonthlyTrend(int[] monthlyValues) {
        // 현재 월 기준으로 최근 12개월(오래된 월 -> 최신 월) 추이 생성
        YearMonth startMonth = YearMonth.now().minusMonths(monthlyValues.length - 1L);
        List<RegionInfraStatisticsResponse.MonthlyAveragePrice> trend = new ArrayList<>(monthlyValues.length);

        for (int i = 0; i < monthlyValues.length; i++) {
            YearMonth yearMonth = startMonth.plusMonths(i);
            Integer baseYearMonth = yearMonth.getYear() * 100 + yearMonth.getMonthValue();

            trend.add(RegionInfraStatisticsResponse.MonthlyAveragePrice.builder()
                    .baseYearMonth(baseYearMonth)
                    .averagePriceThousandWon(monthlyValues[i])
                    .build());
        }

        return trend;
    }
}
