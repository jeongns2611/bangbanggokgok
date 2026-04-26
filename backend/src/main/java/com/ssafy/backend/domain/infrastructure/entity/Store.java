package com.ssafy.backend.domain.infrastructure.entity;

import com.ssafy.backend.domain.code.entity.StoreSmallCategory;
import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

/**
 * 상점 엔티티.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "store")
public class Store extends BaseEntity {

    /**
     * 상점 ID (PK)
     * BIGSERIAL
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 법정동 코드
     * CHAR(10)
     */
    @Column(name = "region_code", length = 10, nullable = false)
    private String regionCode;

    /**
     * 업종 소분류 (FK)
     * CHAR(6)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code", nullable = false)
    private StoreSmallCategory category;

    /**
     * 상점명
     * VARCHAR(100)
     */
    @Column(name = "store_name", length = 100, nullable = false)
    private String storeName;

    /**
     * 도로명 주소
     * VARCHAR(200)
     */
    @Column(name = "road_address", length = 200, nullable = false)
    private String roadAddress;

    /**
     * 위치 정보
     * GEOMETRY(Point, 4326)
     */
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(
            name = "location",
            columnDefinition = "geometry(Point, 4326) GENERATED ALWAYS AS (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)) STORED",
            insertable = false,
            updatable = false
    )
    private Point location;

    /**
     * 위도
     * DOUBLE PRECISION
     */
    @Column(name = "latitude", columnDefinition = "double precision", nullable = false)
    private Double latitude;

    /**
     * 경도
     * DOUBLE PRECISION
     */
    @Column(name = "longitude", columnDefinition = "double precision", nullable = false)
    private Double longitude;
}
