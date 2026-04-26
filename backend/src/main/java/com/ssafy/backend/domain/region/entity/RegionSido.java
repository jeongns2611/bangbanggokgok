package com.ssafy.backend.domain.region.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시도 정보를 담는 엔티티입니다.
 * (예: 서울특별시, 경기도 등)
 */
@Entity
@Getter
@Table(
    name = "region_sido",
    indexes = {
        @Index(name = "idx_sido_name", columnList = "sido_name", unique = true)
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionSido extends BaseEntity {

    /**
     * 시도 코드 (PK)
     * CHAR(2)
     */
    @Id
    @Column(name = "sido_code", columnDefinition = "CHAR(2)")
    private String sidoCode;

    /**
     * 시도명
     * VARCHAR(20)
     */
    @Column(name = "sido_name", nullable = false, length = 20)
    private String sidoName;
}
