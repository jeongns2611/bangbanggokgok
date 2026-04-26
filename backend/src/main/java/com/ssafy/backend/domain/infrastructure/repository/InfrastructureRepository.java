package com.ssafy.backend.domain.infrastructure.repository;

import com.ssafy.backend.domain.infrastructure.dto.response.InfrastructureMarkerRow;
import com.ssafy.backend.domain.infrastructure.dto.response.RegionStoreInfrastructureCount;
import com.ssafy.backend.domain.infrastructure.dto.response.InfrastructureSummaryRow;

import java.util.List;


public interface InfrastructureRepository {

    List<InfrastructureSummaryRow> findInfrastructureSummary(Long houseId, int radiusMeters);
    List<InfrastructureMarkerRow> findInfrastructureMarkers(Long houseId, int radiusMeters);

    /**
     * 시군구 코드 기준으로 store 테이블의 생활 인프라 카테고리별 개수를 조회
     * - CONVENIENCE, CAFE, HOSPITAL, LAUNDRY
     */
    RegionStoreInfrastructureCount findStoreCountsBySigunguCode(String sigunguCode);
}

