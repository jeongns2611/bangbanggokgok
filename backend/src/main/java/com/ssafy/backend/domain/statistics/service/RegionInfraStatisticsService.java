package com.ssafy.backend.domain.statistics.service;

import com.ssafy.backend.domain.statistics.dto.request.RegionInfraStatisticsRequest;
import com.ssafy.backend.domain.statistics.dto.response.RegionInfraStatisticsResponse;

public interface RegionInfraStatisticsService {

    /**
     * 지역 기준 생활 통계를 조회
     * - 생활비 지수: 현재 하드코딩 Provider 사용
     * - 생활 인프라: store 테이블 기반 집계
     * - 지역 안전도: region_safety_stat 테이블 사용
     */
    RegionInfraStatisticsResponse getRegionInfraStatistics(RegionInfraStatisticsRequest request);
}
