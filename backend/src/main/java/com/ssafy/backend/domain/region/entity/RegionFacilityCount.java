package com.ssafy.backend.domain.region.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지역별 편의시설 집계를 담는 엔티티입니다.
 */
@Entity
@Getter
@Table(name = "region_facility_count")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionFacilityCount extends BaseEntity {

    /**
     * 복합키 (시군구 코드, 편의시설 코드)
     */
    @EmbeddedId
    private RegionFacilityCountId id;

    /**
     * 편의시설 개수
     */
    @Column(name = "facility_count", nullable = false)
    private Integer facilityCount;
}
