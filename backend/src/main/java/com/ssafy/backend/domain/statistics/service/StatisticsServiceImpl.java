package com.ssafy.backend.domain.statistics.service;

import com.ssafy.backend.domain.region.repository.RegionSigunguRepository;
import com.ssafy.backend.domain.statistics.dto.response.RegionMonthlyStatisticResponse;
import com.ssafy.backend.domain.statistics.dto.response.StatisticTrendDto;
import com.ssafy.backend.domain.statistics.entity.RegionMonthlyStatistic;
import com.ssafy.backend.domain.statistics.repository.RegionMonthlyStatisticRepository;
import com.ssafy.backend.global.cacheable.CacheStrategy;
import com.ssafy.backend.global.cacheable.MyCacheable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * StatisticsService의 구현체로, 리포지토리로부터 통계 데이터를 조회하고 
 * 캐싱 처리 및 DTO 변환을 수행합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {

    private final RegionMonthlyStatisticRepository repository;
    private final RegionSigunguRepository regionSigunguRepository;

    /**
     * 특정 시군구의 월별 부동산 통계를 조회합니다.
     * Look-aside 캐싱 전략을 사용하여 반복적인 DB 접근을 최소화하며,
     * 데이터는 1주일(604,800초) 동안 캐싱됩니다.
     *
     * @param sidoName    시/도 이름
     * @param sigunguName 시/군/구 이름
     * @return 월별 통계 추이가 포함된 응답 DTO
     */
    @Override
    @MyCacheable(cacheStrategy = CacheStrategy.LOOK_ASIDE, cacheName = "region_monthly_statistics", key = "#sidoName + ':' + #sigunguName", ttlSeconds = 604800)
    public RegionMonthlyStatisticResponse getMonthlyRegionStatistics(String sidoName, String sigunguName) {
        // 시도/시군구 이름으로 시군구 코드 조회
        String sigunguCode = regionSigunguRepository.findSigunguCodeByNames(sidoName, sigunguName)
                .orElseThrow(() -> new IllegalArgumentException("해당 지역을 찾을 수 없습니다: " + sidoName + " " + sigunguName));

        // 리포지토리에서 해당 지역의 통계를 최신순(년월 내림차순)으로 조회
        List<RegionMonthlyStatistic> stats = repository.findBySigunguCodeOrderByBaseYearMonthDesc(sigunguCode);

        // 엔티티 리스트를 반환용 DTO 리스트로 변환
        List<StatisticTrendDto> trends = stats.stream()
                .map(stat -> StatisticTrendDto.builder()
                        .baseYearMonth(stat.getBaseYearMonth())
                        .avgJeonSae(stat.getAvgJeonse())
                        .avgRentDeposit(stat.getAvgRentDeposit())
                        .avgRent(stat.getAvgMonthlyCost())
                        .transactionCount(stat.getTradeCount())
                        .build())
                .collect(Collectors.toList());

        // 빌더 패턴을 사용하여 최종 응답 객체 생성
        return RegionMonthlyStatisticResponse.builder()
                .trends(trends)
                .build();
    }
}
