package com.ssafy.backend.domain.region.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 법정동 정보를 담는 엔티티입니다.
 * (시도-시군구-읍면동 계층 구조)
 */
@Entity
@Getter
@Table(
    name = "region",
    indexes = {
        @Index(name = "idx_sido_sigungu_dong_name", columnList = "sido_code, sigungu_code, dong_name", unique = true)
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region extends BaseEntity {

    /**
     * 법정동 코드 (PK)
     * CHAR(10)
     */
    @Id
    @Column(name = "region_code", columnDefinition = "CHAR(10)")
    private String regionCode;

    /**
     * 시도 정보 (FK)
     * CHAR(2)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sido_code", nullable = false)
    private RegionSido regionSido;

    /**
     * 시군구 정보 (FK)
     * CHAR(5)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sigungu_code", nullable = false)
    private RegionSigungu regionSigungu;

    /**
     * 읍면동명
     * VARCHAR(20)
     */
    @Column(name = "dong_name", length = 20)
    private String dongName;
}
