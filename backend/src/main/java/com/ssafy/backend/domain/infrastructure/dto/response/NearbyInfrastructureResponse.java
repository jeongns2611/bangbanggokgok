package com.ssafy.backend.domain.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 매물 주변 인프라 조회 API의 최종 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class NearbyInfrastructureResponse {

    private Long houseId;
    private Integer radiusMeters;
    private List<Summary> summaries;
    private List<Marker> markers;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Summary {
        private String type;
        private Integer count;
        // 가장 가까운 시설까지의 거리(m), 없으면 null
        private Integer nearestDistanceMeters;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Marker {
        private Long id;
        private String type;
        private String name;
        private Double latitude;
        private Double longitude;
    }
}

