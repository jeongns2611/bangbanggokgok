package com.ssafy.backend.domain.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 인프라 집계 결과를 전달하는 내부 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class InfrastructureMarkerRow {

    // 시설 ID
    private Long id;
    // 시설 타입 코드
    private String type;
    // 시설 이름
    private String name;
    // 위도
    private Double latitude;
    // 경도
    private Double longitude;
}

