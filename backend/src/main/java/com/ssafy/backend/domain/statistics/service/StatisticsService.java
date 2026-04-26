package com.ssafy.backend.domain.statistics.service;

import com.ssafy.backend.domain.statistics.dto.response.RegionMonthlyStatisticResponse;

/**
 * 부동산 통계 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 */
public interface StatisticsService {
    /**
     * 특정 시군구의 월별 부동산 통계를 조회합니다.
     *
     * @param sidoName    시/도 이름
     * @param sigunguName 시/군/구 이름
     * @return 월별 통계 응답 DTO
     */
    RegionMonthlyStatisticResponse getMonthlyRegionStatistics(String sidoName, String sigunguName);
}
