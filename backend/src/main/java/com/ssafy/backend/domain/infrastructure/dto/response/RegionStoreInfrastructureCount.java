package com.ssafy.backend.domain.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
/**
 * 시군구 기준 생활 인프라(store) 카테고리별 집계 DTO
 */
public class RegionStoreInfrastructureCount {

    private Integer convenienceStoreCount;
    private Integer cafeCount;
    private Integer hospitalCount;
    private Integer laundryCount;
}
