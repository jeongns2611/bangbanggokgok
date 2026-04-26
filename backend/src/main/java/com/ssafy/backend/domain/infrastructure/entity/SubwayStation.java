package com.ssafy.backend.domain.infrastructure.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

/**
 * 지하철역 엔티티.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "subway_station")
public class SubwayStation extends BaseEntity {

    /**
     * 역 ID (PK)
     * SERIAL
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 외부 데이터셋에서 제공하는 역 ID (code 값)
     * VARCHAR(20)
     */
    @Column(name = "external_station_id", length = 20, nullable = false, unique = true)
    private String externalStationId;

    /**
     * 역명
     * VARCHAR(20)
     */
    @Column(name = "station_name", length = 20, nullable = false)
    private String stationName;

    /**
     * 호선명
     * VARCHAR(10)
     */
    @Column(name = "line_name", length = 10, nullable = false)
    private String lineName;

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
