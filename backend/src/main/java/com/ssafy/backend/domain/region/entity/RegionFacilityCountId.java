package com.ssafy.backend.domain.region.entity;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * RegionFacilityCount 엔티티의 복합키 클래스입니다.
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RegionFacilityCountId implements Serializable {

    /**
     * 시군구 코드
     * CHAR(5)
     */
    @Column(name = "sigungu_code", columnDefinition = "CHAR(5)")
    private String sigunguCode;

    /**
     * 편의시설 코드
     * INTEGER
     */
    @Column(name = "facility_code")
    private Integer facilityCode;
}
