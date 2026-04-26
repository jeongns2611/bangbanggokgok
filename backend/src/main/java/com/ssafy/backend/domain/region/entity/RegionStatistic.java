package com.ssafy.backend.domain.region.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시군구별 평균 지표 (평균 월세, 통근 시간 등)를 담는 엔티티입니다.
 */
@Entity
@Getter
@Table(name = "region_statistic")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionStatistic extends BaseEntity {

    /**
     * 시군구 코드 (PK)
     * CHAR(5)
     */
    @Id
    @Column(name = "sigungu_code", columnDefinition = "CHAR(5)")
    private String sigunguCode;

    /**
     * 월세 평균 보증금
     */
    @Column(name = "wolsae_avg_deposit", nullable = false)
    private Integer wolsaeAvgDeposit;

    /**
     * 월세 평균 월지출
     */
    @Column(name = "wolsae_avg_monthly_cost", nullable = false)
    private Integer wolsaeAvgMonthlyCost;

    /**
     * 평균 통근시간
     */
    @Column(name = "avg_commute_time", nullable = false)
    private Integer avgCommuteTime;

    /**
     * 생활시설 개수
     */
    @Column(name = "facility_count", nullable = false)
    private Integer facilityCount;

    /**
     * 공급예상 매물개수
     */
    @Column(name = "expected_supply_count", nullable = false)
    private Integer expectedSupplyCount;

    /**
     * 평균 삼겹살 식비 지수
     */
    @Column(name = "pork_belly_cost", nullable = false)
    private Integer porkBellyCost;

    /**
     * 평균 김밥 식비 지수
     */
    @Column(name = "kimbap_cost", nullable = false)
    private Integer kimbapCost;

    /**
     * 데이터 기준 일자
     */
    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;
}
