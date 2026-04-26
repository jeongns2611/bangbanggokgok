package com.ssafy.backend.domain.region.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시계열 추이 데이터를 담는 엔티티입니다.
 */
@Entity
@Getter
@Table(name = "region_monthly_trend")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionMonthlyTrend extends BaseEntity {

    /**
     * 시계열 통계 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 시군구 코드
     * CHAR(5)
     */
    @Column(name = "sigungu_code", nullable = false, columnDefinition = "CHAR(5)")
    private String sigunguCode;

    /**
     * 지역구/행정동 코드
     * VARCHAR(10)
     */
    @Column(name = "sgg_cd", nullable = false, length = 10)
    private String sggCd;

    /**
     * 기준년월
     * INTEGER (YYYYMM format likely)
     */
    @Column(name = "base_year_month", nullable = false)
    private Integer baseYearMonth;

    /**
     * 월별 평균 보증금
     */
    @Column(name = "avg_deposit", nullable = false)
    private Integer avgDeposit;

    /**
     * 월별 평균 월세
     */
    @Column(name = "avg_monthly_cost", nullable = false)
    private Integer avgMonthlyCost;

    /**
     * 월별 거래 건수
     */
    @Column(name = "trade_count", nullable = false)
    private Integer tradeCount;
}
