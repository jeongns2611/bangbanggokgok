package com.ssafy.backend.domain.statistics.service;

import com.ssafy.backend.domain.infrastructure.dto.response.RegionStoreInfrastructureCount;
import com.ssafy.backend.domain.infrastructure.service.InfrastructureService;
import com.ssafy.backend.domain.region.service.RegionService;
import com.ssafy.backend.domain.statistics.dto.request.RegionInfraStatisticsRequest;
import com.ssafy.backend.domain.statistics.dto.response.RegionInfraStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionInfraStatisticsServiceImpl implements RegionInfraStatisticsService {

    private final RegionService regionService;
    private final InfrastructureService infrastructureService;
    private final LivingCostIndexProvider livingCostIndexProvider;
    private final RegionSafetyProvider regionSafetyProvider;

    @Override
    public RegionInfraStatisticsResponse getRegionInfraStatistics(RegionInfraStatisticsRequest request) {
        // 입력 문자열의 앞뒤 공백 제거 후 지역 코드 조회
        String sidoName = request.getSidoName().trim();
        String sigunguName = request.getSigunguName().trim();
        String sigunguCode = regionService.getSigunguCode(sidoName, sigunguName);

        // 생활 인프라는 store 테이블 기반으로 조회
        RegionStoreInfrastructureCount infrastructureCount =
                infrastructureService.getStoreCountsBySigunguCode(sigunguCode);

        // 하드코딩 Provider + DB 집계를 결합해 최종 응답 생성
                return RegionInfraStatisticsResponse.builder()
                .region(RegionInfraStatisticsResponse.Region.builder()
                        .sidoName(sidoName)
                        .sigunguName(sigunguName)
                        .sigunguCode(sigunguCode)
                        .build())
                .livingCostIndex(livingCostIndexProvider.getLivingCostIndex(sigunguCode))
                .livingInfrastructure(RegionInfraStatisticsResponse.LivingInfrastructure.builder()
                        .convenienceStoreCount(defaultZero(infrastructureCount.getConvenienceStoreCount()))
                        .cafeCount(defaultZero(infrastructureCount.getCafeCount()))
                        .hospitalCount(defaultZero(infrastructureCount.getHospitalCount()))
                        .laundryCount(defaultZero(infrastructureCount.getLaundryCount()))
                        .build())
                .regionSafety(regionSafetyProvider.getRegionSafety(sigunguCode))
                .build();
    }

    // null 집계값이 들어오면 응답에서 0으로 보정
    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }
}
