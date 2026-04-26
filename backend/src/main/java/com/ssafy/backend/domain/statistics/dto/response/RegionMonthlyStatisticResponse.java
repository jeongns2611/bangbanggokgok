package com.ssafy.backend.domain.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 특정 지역의 월별 통계 데이터를 담는 응답 DTO입니다.
 * 시간 흐름에 따른 보증금, 월세, 거래량의 변화를 리스트 형태로 포함합니다.
 */
@Getter
@Builder
@AllArgsConstructor
public class RegionMonthlyStatisticResponse {
    /** 월별 통계 추이 리스트 */
    private List<StatisticTrendDto> trends;
}
