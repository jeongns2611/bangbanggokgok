package com.ssafy.backend.domain.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 인프라 요약 결과를 전달하는 내부 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class InfrastructureSummaryRow {

    // 시설 타입
    private String type;
    // 반경 내 시설 개수
    private Integer count;
    // 가장 가까운 시설까지의 거리(m), 없으면 null
    private Integer nearestDistanceMeters;
}

