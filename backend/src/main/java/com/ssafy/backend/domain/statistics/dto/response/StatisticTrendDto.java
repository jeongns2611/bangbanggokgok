package com.ssafy.backend.domain.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 월별 통계 지표(보증금, 월세, 거래량)를 담는 상세 DTO입니다.
 */
@Getter
@Builder
@AllArgsConstructor
public class StatisticTrendDto {
    /** 기준 년월 (예: 202407) */
    private Integer baseYearMonth;

    /** 해당 월의 평균 전세가 (만원) */
    private Integer avgJeonSae;

    /** 해당 월의 평균 월세 보증금 (만원) */
    private Integer avgRentDeposit;

    /** 해당 월의 평균 월세 (만원) */
    private Integer avgRent;           // Mapped from avgMonthlyCost

    /** 해당 월의 총 실거래 건수 */
    private Integer transactionCount;  // Mapped from tradeCount
}
