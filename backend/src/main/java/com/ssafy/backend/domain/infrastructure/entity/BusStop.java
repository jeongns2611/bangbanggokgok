package com.ssafy.backend.domain.infrastructure.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.LocalDate;

/**
 * 버스 정류장 엔티티.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "bus_stop")
public class BusStop extends BaseEntity {

    /**
     * 정류장 ID (PK)
     * SERIAL
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 노드 ID
     * VARCHAR(20)
     */
    @Column(name = "node_id", length = 20, nullable = false)
    private String nodeId;

    /**
     * 정류장 번호
     * VARCHAR(20)
     */
    @Column(name = "station_no", length = 20, nullable = false)
    private String stationNo;

    /**
     * 정류장명
     * VARCHAR(100)
     */
    @Column(name = "station_name", length = 100, nullable = false)
    private String stationName;

    /**
     * 정류장 유형
     * VARCHAR(50)
     */
    @Column(name = "station_type", length = 50)
    private String stationType;

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

    /**
     * 기준일
     * DATE
     */
    @Column(name = "standard_date", nullable = false)
    private LocalDate standardDate;

}
