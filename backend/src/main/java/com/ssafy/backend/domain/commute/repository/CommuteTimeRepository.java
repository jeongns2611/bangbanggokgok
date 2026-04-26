package com.ssafy.backend.domain.commute.repository;

import com.ssafy.backend.domain.commute.entity.CommuteTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 통근 시간 DB 캐시 레포지토리 (commute_time 테이블).
 *
 * ODsay API 호출 결과를 DB에 캐싱하여, 동일한 origin/dest 좌표 조합에 대해
 * 반복 API 호출을 방지한다.
 *
 * [테이블 스키마] commute_time
 *   - id            : SERIAL (PK)
 *   - origin        : GEOMETRY(Point, 4326) — 출발지(매물) 좌표
 *   - dest          : GEOMETRY(Point, 4326) — 도착지(직장) 좌표
 *   - total_time    : REAL — 총 소요 시간 (분)
 *   - transit_count : INTEGER — 환승 횟수
 *   - cost          : INTEGER — 요금
 *   - total_distance: REAL — 총 이동 거리 (km, 소수점 1자리) ← 신규 컬럼
 */
public interface CommuteTimeRepository extends JpaRepository<CommuteTime, Long> {

    /**
     * 출발/도착 좌표가 소수점 6자리 이내에서 일치하는 통근 시간 캐시를 조회한다.
     *
     * [버그 수정 ①] 좌표 완전일치(=) → ROUND(...::numeric, 6) 비교로 변경
     *   - GPS 정밀도(±0.1m)에서 발생하는 소수점 오차를 흡수한다.
     *   - 예: 127.0280000001 ≈ 127.028000 → 캐시 hit
     *
     * [버그 수정 ②] ORDER BY 없이 LIMIT 1 → ORDER BY ct.id DESC LIMIT 1 로 변경
     *   - 동일 origin/dest에 중복 row가 쌓인 경우, 가장 최신 row를 일관되게 반환한다.
     *
     * @param originX 출발지 경도 (longitude)
     * @param originY 출발지 위도 (latitude)
     * @param destX   도착지 경도 (longitude)
     * @param destY   도착지 위도 (latitude)
     * @return 캐시 hit 시 CommuteTime, miss 시 Optional.empty()
     */
    @Query(value = """
            SELECT *
            FROM commute_time ct
            WHERE ROUND(ST_X(ct.origin)::numeric, 6) = ROUND(CAST(:originX AS numeric), 6)
              AND ROUND(ST_Y(ct.origin)::numeric, 6) = ROUND(CAST(:originY AS numeric), 6)
              AND ROUND(ST_X(ct.dest)::numeric,   6) = ROUND(CAST(:destX   AS numeric), 6)
              AND ROUND(ST_Y(ct.dest)::numeric,   6) = ROUND(CAST(:destY   AS numeric), 6)
            ORDER BY ct.id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<CommuteTime> findByCoordinates(
            @Param("originX") double originX,
            @Param("originY") double originY,
            @Param("destX") double destX,
            @Param("destY") double destY
    );
}
