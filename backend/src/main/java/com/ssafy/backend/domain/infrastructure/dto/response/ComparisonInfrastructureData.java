package com.ssafy.backend.domain.infrastructure.dto.response;

/**
 * house compare 화면/AI 비교용으로 주변 인프라 정보를 묶어 전달하는 DTO.
 * InfrastructureService가 원시 summary 결과를 비교 도메인에서 쓰기 쉬운 형태로 변환할 때 사용한다.
 */
public record ComparisonInfrastructureData(
        InfraCount infraCount,
        MinDist minDist
) {

    /**
     * 비교 점수 계산에 사용하는 주변 인프라 개수 정보.
     */
    public record InfraCount(
            Integer convenienceStoreCount,
            Integer laundryCount,
            Integer cafeCount,
            Integer hospitalCount,
            Integer pharmacyCount,
            Integer busStopCount
    ) {
    }

    /**
     * 비교 점수 계산에 사용하는 가장 가까운 인프라 거리 정보.
     */
    public record MinDist(
            Integer convenienceDist,
            Integer laundryDist,
            Integer cafeDist,
            Integer hospitalDist,
            Integer pharmacyDist,
            Integer subwayDist
    ) {
    }
}
