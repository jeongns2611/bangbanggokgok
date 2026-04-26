package com.ssafy.backend.domain.statistics.service;

import com.ssafy.backend.domain.statistics.dto.response.RegionInfraStatisticsResponse;

public interface LivingCostIndexProvider {

    /**
     * TODO: 특정 시군구의 생활비 지수 데이터를 제공 (추후 DB/외부 API 구현으로 변경)
     */
    RegionInfraStatisticsResponse.LivingCostIndex getLivingCostIndex(String sigunguCode);
}
