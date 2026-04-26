package com.ssafy.backend.domain.statistics.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

/**
 * 시군구별 월별 부동산 거래 통계 데이터를 저장하는 엔티티입니다.
 * 'region_monthly_trend' 테이블과 매핑되며, 자주 조회되는 시군구 코드와 기준 년월에 인덱스가 설정되어 있습니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "region_monthly_trend", indexes = {
        @Index(name = "idx_sigungu_code", columnList = "sigungu_code"),
        @Index(name = "idx_base_year_month", columnList = "base_year_month")
})
public class RegionMonthlyStatistic {

    /** 식별자 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 법정동 시군구 코드 (5자리) */
    @Column(name = "sigungu_code", nullable = false, length = 5)
    private String sigunguCode;

    /** 행정구역 코드 (10자리) */
    @Column(name = "sgg_cd", nullable = false, length = 10)
    private String sggCd;

    /** 통계 기준 년월 (YYYYMM) */
    @Column(name = "base_year_month", nullable = false)
    private Integer baseYearMonth;

    /** 해당 월의 평균 전세가 (만원) */
    @Column(name = "avg_jeonse", nullable = false)
    private Integer avgJeonse;

    /** 해당 월의 평균 월세 보증금 (만원) */
    @Column(name = "avg_rent_deposit", nullable = false)
    private Integer avgRentDeposit;

    /** 해당 월의 평균 월세액 (만원) */
    @Column(name = "avg_monthly_cost", nullable = false)
    private Integer avgMonthlyCost;

    /** 해당 월의 총 거래 건수 */
    @Column(name = "trade_count", nullable = false)
    private Integer tradeCount;

    /** 데이터 생성 일시 (JPA Auditing) */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT NOW()")
    private OffsetDateTime createdAt;

    /** 데이터 수정 일시 (JPA Auditing) */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT NOW()")
    private OffsetDateTime updatedAt;

    @Builder
    public RegionMonthlyStatistic(String sigunguCode, String sggCd, Integer baseYearMonth, 
                                  Integer avgJeonse, Integer avgRentDeposit, Integer avgMonthlyCost, Integer tradeCount) {
        this.sigunguCode = sigunguCode;
        this.sggCd = sggCd;
        this.baseYearMonth = baseYearMonth;
        this.avgJeonse = avgJeonse;
        this.avgRentDeposit = avgRentDeposit;
        this.avgMonthlyCost = avgMonthlyCost;
        this.tradeCount = tradeCount;
    }
}
