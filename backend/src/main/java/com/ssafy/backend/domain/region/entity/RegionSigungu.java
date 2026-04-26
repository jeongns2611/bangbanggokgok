package com.ssafy.backend.domain.region.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시군구 정보를 담는 엔티티입니다.
 * (예: 강남구, 종로구 등)
 */
@Entity
@Getter
@Table(
    name = "region_sigungu",
    indexes = {
        @Index(name = "idx_sido_sigungu_name", columnList = "sido_code, sigungu_name", unique = true)
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionSigungu extends BaseEntity {

    /**
     * 시군구 코드 (PK)
     * CHAR(5)
     */
    @Id
    @Column(name = "sigungu_code", columnDefinition = "CHAR(5)")
    private String sigunguCode;

    /**
     * 시도 코드 (FK)
     * CHAR(2)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sido_code", nullable = false)
    private RegionSido regionSido;

    /**
     * 시군구 이름
     * VARCHAR(20)
     */
    @Column(name = "sigungu_name", nullable = false, length = 20)
    private String sigunguName;
}
