package com.ssafy.backend.domain.infrastructure.service;

import com.ssafy.backend.domain.house.entity.CurrentHouse;
import com.ssafy.backend.domain.house.repository.CurrentHouseRepository;
import com.ssafy.backend.domain.infrastructure.dto.response.InfrastructureMarkerRow;
import com.ssafy.backend.domain.infrastructure.dto.response.RegionStoreInfrastructureCount;
import com.ssafy.backend.domain.infrastructure.dto.response.InfrastructureSummaryRow;
import com.ssafy.backend.domain.infrastructure.dto.response.NearbyInfrastructureResponse;
import com.ssafy.backend.domain.infrastructure.repository.InfrastructureRepository;
import com.ssafy.backend.domain.infrastructure.type.InfrastructureType;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InfrastructureService {

    private static final int RADIUS_METERS = 800;

    private final CurrentHouseRepository currentHouseRepository;
    private final InfrastructureRepository infrastructureRepository;

    /**
     * 특정 매물 기준 반경 내 인프라 정보 조회 메서드
     *  - summary는 7개 타입을 항상 포함
     *  - marker는 없으면 빈 배열을 반환
     */
    public NearbyInfrastructureResponse getNearbyInfrastructures(Long houseId) {
        // 1) 실매물 검증
        validateHouse(houseId);

        // 2) 인프라 정보 요약 및 집계
        List<InfrastructureSummaryRow> summaryRows =
                infrastructureRepository.findInfrastructureSummary(houseId, RADIUS_METERS);
        List<InfrastructureMarkerRow> markerRows =
                infrastructureRepository.findInfrastructureMarkers(houseId, RADIUS_METERS);

        // 3) 반환 형식 일치 작업
        List<NearbyInfrastructureResponse.Summary> summaries = buildFixedSummaries(summaryRows);
        List<NearbyInfrastructureResponse.Marker> markers = buildMarkers(markerRows);

        return NearbyInfrastructureResponse.builder()
                .houseId(houseId)
                .radiusMeters(RADIUS_METERS)
                .summaries(summaries)
                .markers(markers)
                .build();
    }

    public RegionStoreInfrastructureCount getStoreCountsBySigunguCode(String sigunguCode) {
        // 지역(시군구) 기준 생활 인프라 집계 조회
        return infrastructureRepository.findStoreCountsBySigunguCode(sigunguCode);
    }

    private void validateHouse(Long houseId) {
        CurrentHouse house = currentHouseRepository.findById(houseId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.HOUSE_NOT_FOUND,
                        "존재하지 않는 매물입니다. houseId=" + houseId
                ));

        if (house.getDeletedAt() != null) {
            throw new BusinessException(
                    ErrorCode.HOUSE_NOT_FOUND,
                    "삭제된 매물입니다. houseId=" + houseId
            );
        }

        if (house.getPosition() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "매물 위치 좌표가 없어 주변 인프라를 조회할 수 없습니다. houseId=" + houseId
            );
        }
    }

    private List<NearbyInfrastructureResponse.Summary> buildFixedSummaries(List<InfrastructureSummaryRow> rows) {
        Map<String, InfrastructureSummaryRow> rowByType = rows == null
                ? Collections.emptyMap()
                : rows.stream().collect(Collectors.toMap(
                        InfrastructureSummaryRow::getType,
                        Function.identity(),
                        // 동일 타입이 중복으로 들어오면 첫 번째 값을 유지함
                        (left, right) -> left
                ));

        return Arrays.stream(InfrastructureType.values())
                .map(type -> {
                    InfrastructureSummaryRow row = rowByType.get(type.getCode());
                    if (row == null) {
                        return NearbyInfrastructureResponse.Summary.builder()
                                .type(type.getCode())
                                .count(0)
                                .nearestDistanceMeters(null)
                                .build();
                    }

                    return NearbyInfrastructureResponse.Summary.builder()
                            .type(type.getCode())
                            .count(row.getCount() == null ? 0 : row.getCount())
                            .nearestDistanceMeters(row.getNearestDistanceMeters())
                            .build();
                })
                .toList();
    }

    private List<NearbyInfrastructureResponse.Marker> buildMarkers(List<InfrastructureMarkerRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        return rows.stream()
                .map(row -> NearbyInfrastructureResponse.Marker.builder()
                        .id(row.getId())
                        .type(row.getType())
                        .name(row.getName())
                        .latitude(row.getLatitude())
                        .longitude(row.getLongitude())
                        .build())
                .toList();
    }
}

