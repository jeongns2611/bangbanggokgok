package com.ssafy.backend.domain.safety.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시군구 단위 안전 통계 정보를 저장하는 엔티티입니다.
 * (면적, CCTV 수, 가로등 수, 경찰시설 수)
 */
@Entity
@Getter
@Table(name = "region_safety_stat")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionSafetyStat extends BaseEntity {

    /**
     * 시군구 코드 (PK)
     * CHAR(5)
     */
    @Id
    @Column(name = "sigungu_code", columnDefinition = "CHAR(5)")
    private String sigunguCode;

    /**
     * 면적(km^2)
     * DOUBLE PRECISION
     */
    @Column(name = "area_km2", nullable = false)
    private Double areaKm2;

    /**
     * CCTV 개수
     */
    @Column(name = "cctv_count", nullable = false)
    private Integer cctvCount;

    /**
     * 가로등 개수
     */
    @Column(name = "streetlight_count", nullable = false)
    private Integer streetlightCount;

    /**
     * 경찰시설 개수
     */
    @Column(name = "police_facility_count", nullable = false)
    private Integer policeFacilityCount;
}
