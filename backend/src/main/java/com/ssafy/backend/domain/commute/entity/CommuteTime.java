package com.ssafy.backend.domain.commute.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

/**
 * 통근 시간 엔티티.
 *
 * ODsay API 호출 결과를 DB에 캐싱하여, 동일한 origin/dest 좌표 조합에 대해
 * 반복 외부 API 호출을 방지한다.
 *
 * [테이블: commute_time]
 *   - id            : SERIAL (PK)
 *   - origin        : GEOMETRY(Point, 4326) — 출발지(매물) 좌표
 *   - dest          : GEOMETRY(Point, 4326) — 도착지(직장) 좌표
 *   - total_time    : REAL — 총 소요 시간 (분)
 *   - transit_count : INTEGER — 환승 횟수
 *   - cost          : INTEGER — 요금
 *   - total_distance: REAL — 총 이동 거리 (km, 소수점 1자리)
 *                     ※ 신규 컬럼. DDL 실행 필요:
 *                       ALTER TABLE commute_time ADD COLUMN total_distance REAL;
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "commute_time")
public class CommuteTime extends BaseEntity {

    /**
     * 통근 시간 ID (PK)
     * SERIAL
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 출발지
     * GEOMETRY(Point, 4326)
     */
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(name = "origin", columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point origin;

    /**
     * 도착지
     * GEOMETRY(Point, 4326)
     */
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(name = "dest", columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point dest;

    /**
     * 총 소요 시간 (분)
     * REAL
     */
    @Column(name = "total_time", nullable = false)
    private Float totalTime;

    /**
     * 환승 횟수
     * INTEGER
     */
    @Column(name = "transit_count", nullable = false)
    private Integer transitCount;

    /**
     * 비용 (원)
     * INTEGER
     */
    @Column(name = "cost", nullable = false)
    private Integer cost;

    /**
     * 총 이동 거리 (km, 소수점 1자리).
     * ODsay API 응답의 totalDistance(m)를 km 환산 후 저장한다.
     *
     * 기존 레코드에는 이 컬럼이 없으므로 nullable. 신규 저장부터 값이 채워진다.
     * DDL: ALTER TABLE commute_time ADD COLUMN total_distance REAL;
     */
    @Column(name = "total_distance")
    private Float totalDistance;

    @Builder
    private CommuteTime(
            Point origin,
            Point dest,
            Float totalTime,
            Integer transitCount,
            Integer cost,
            Float totalDistance
    ) {
        this.origin = origin;
        this.dest = dest;
        this.totalTime = totalTime;
        this.transitCount = transitCount;
        this.cost = cost;
        this.totalDistance = totalDistance;
    }

    /**
     * 팩토리 메서드 — 새 통근시간 캐시 엔티티를 생성한다.
     *
     * @param origin         출발지(매물) 좌표
     * @param dest           도착지(직장) 좌표
     * @param totalTime      총 소요 시간 (분)
     * @param transitCount   환승 횟수 (버스+지하철)
     * @param cost           요금 (원)
     * @param totalDistance  총 이동 거리 (km 단위, 소수점 1자리)
     */
    public static CommuteTime of(
            Point origin,
            Point dest,
            Float totalTime,
            Integer transitCount,
            Integer cost,
            Float totalDistance
    ) {
        return CommuteTime.builder()
                .origin(origin)
                .dest(dest)
                .totalTime(totalTime)
                .transitCount(transitCount)
                .cost(cost)
                .totalDistance(totalDistance)
                .build();
    }
}
