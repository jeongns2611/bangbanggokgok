package com.ssafy.backend.domain.region.service;

import com.ssafy.backend.domain.house.dto.response.HouseCompareResponse;
import com.ssafy.backend.domain.region.entity.RegionStatistic;
import com.ssafy.backend.domain.region.repository.RegionStatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * house compare에서 필요한 지역 통계를 조회해 비교용 DongStats로 변환하는 서비스.
 * 현재는 region_statistic의 식비 지표만 사용하고, 안전 관련 값은 기본값으로 채운다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionStatisticsLookupService {

    private final RegionStatisticRepository regionStatisticRepository;

    /**
     * 시군구 코드로 지역 통계를 조회하고 compare 응답용 DongStats로 변환한다.
     * 통계가 없으면 0 기반 기본값을 반환한다.
     */
    public HouseCompareResponse.DongStats getDongStats(String sigunguCode) {
        if (sigunguCode == null || sigunguCode.isBlank()) {
            return defaultDongStats();
        }

        return regionStatisticRepository.findById(sigunguCode)
                .map(this::toDongStats)
                .orElseGet(this::defaultDongStats);
    }

    /**
     * RegionStatistic 엔티티를 house compare 응답 DTO로 변환한다.
     */
    private HouseCompareResponse.DongStats toDongStats(RegionStatistic regionStatistic) {
        return new HouseCompareResponse.DongStats(
                0,
                0,
                0,
                0.0,
                regionStatistic.getPorkBellyCost(),
                regionStatistic.getKimbapCost()
        );
    }

    /**
     * 지역 통계가 없을 때 사용하는 기본값.
     */
    private HouseCompareResponse.DongStats defaultDongStats() {
        return new HouseCompareResponse.DongStats(0, 0, 0, 0.0, 0, 0);
    }
}
