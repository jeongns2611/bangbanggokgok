package com.ssafy.backend.domain.infrastructure.repository;

import com.ssafy.backend.domain.infrastructure.dto.response.InfrastructureMarkerRow;
import com.ssafy.backend.domain.infrastructure.dto.response.InfrastructureSummaryRow;
import com.ssafy.backend.domain.infrastructure.dto.response.RegionStoreInfrastructureCount;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class InfrastructureRepositoryImpl implements InfrastructureRepository {

    private final EntityManager entityManager;

    /**
     * 반경 내 인프라 요약 정보를 조회
     *  - 시설 타입별 개수(count)
     *  - 시설 타입별 최단거리(nearestDistanceMeters)
     */
    @Override
    public List<InfrastructureSummaryRow> findInfrastructureSummary(Long houseId, int radiusMeters) {
        String summarySql = """
                WITH house AS (
                    SELECT ch.position
                    FROM current_house ch
                    WHERE ch.id = :houseId
                      AND ch.deleted_at IS NULL
                ),
                store_with_type AS (
                    SELECT
                        s.location,
                        CASE
                            WHEN s.category_code = 'G20405' THEN 'CONVENIENCE'
                            WHEN s.category_code = 'S20901' THEN 'LAUNDRY'
                            WHEN s.category_code = 'I21201' THEN 'CAFE'
                            WHEN s.category_code IN ('G21501', 'G21502') THEN 'PHARMACY'
                            WHEN slc.code = 'Q1' THEN 'HOSPITAL'
                            ELSE NULL
                        END AS type
                    FROM store s
                    JOIN store_small_category ssc ON s.category_code = ssc.code
                    JOIN store_medium_category smc ON ssc.medium_category_code = smc.code
                    JOIN store_large_category slc ON smc.large_category_code = slc.code
                ),
                infra AS (
                    SELECT
                        'BUS' AS type,
                        bs.location AS location
                    FROM bus_stop bs
                    CROSS JOIN house h
                    WHERE ST_DWithin(bs.location::geography, h.position::geography, :radiusMeters)
                
                    UNION ALL
                
                    SELECT
                        'SUBWAY' AS type,
                        ss.location AS location
                    FROM subway_station ss
                    CROSS JOIN house h
                    WHERE ST_DWithin(ss.location::geography, h.position::geography, :radiusMeters)
                
                    UNION ALL
                
                    SELECT
                        swt.type AS type,
                        swt.location AS location
                    FROM store_with_type swt
                    CROSS JOIN house h
                    WHERE swt.type IS NOT NULL
                      AND ST_DWithin(swt.location::geography, h.position::geography, :radiusMeters)
                )
                SELECT
                    i.type AS type,
                    CAST(COUNT(*) AS INTEGER) AS count,
                    CAST(MIN(ST_Distance(i.location::geography, h.position::geography)) AS INTEGER) AS nearest_distance_meters
                FROM infra i
                CROSS JOIN house h
                GROUP BY i.type
                ORDER BY i.type
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(summarySql)
                .setParameter("houseId", houseId)
                .setParameter("radiusMeters", radiusMeters)
                .getResultList();

        return rows.stream()
                .map(this::toSummaryRow)
                .toList();
    }

    /**
     * 반경 내 인프라 집계 정보 조회
     *  - 응답 형식에 맞춰 id, type, name, latitude, longitude만 반환
     *  - 시설 타입 분류 후 거리순으로 정렬하여 반환
     */
    @Override
    public List<InfrastructureMarkerRow> findInfrastructureMarkers(Long houseId, int radiusMeters) {
        String markerSql = """
                WITH house AS (
                    SELECT ch.position
                    FROM current_house ch
                    WHERE ch.id = :houseId
                      AND ch.deleted_at IS NULL
                ),
                store_with_type AS (
                    SELECT
                        s.id,
                        s.store_name,
                        s.latitude,
                        s.longitude,
                        s.location,
                        CASE
                            WHEN s.category_code = 'G20405' THEN 'CONVENIENCE'
                            WHEN s.category_code = 'S20901' THEN 'LAUNDRY'
                            WHEN s.category_code = 'I21201' THEN 'CAFE'
                            WHEN s.category_code IN ('G21501', 'G21502') THEN 'PHARMACY'
                            WHEN slc.code = 'Q1' THEN 'HOSPITAL'
                            ELSE NULL
                        END AS type
                    FROM store s
                    JOIN store_small_category ssc ON s.category_code = ssc.code
                    JOIN store_medium_category smc ON ssc.medium_category_code = smc.code
                    JOIN store_large_category slc ON smc.large_category_code = slc.code
                ),
                marker_source AS (
                    SELECT
                        bs.id AS id,
                        'BUS' AS type,
                        bs.station_name AS name,
                        bs.latitude AS latitude,
                        bs.longitude AS longitude,
                        ST_Distance(bs.location::geography, h.position::geography) AS distance_meters
                    FROM bus_stop bs
                    CROSS JOIN house h
                    WHERE ST_DWithin(bs.location::geography, h.position::geography, :radiusMeters)
                
                    UNION ALL
                
                    SELECT
                        ss.id AS id,
                        'SUBWAY' AS type,
                        ss.station_name AS name,
                        ss.latitude AS latitude,
                        ss.longitude AS longitude,
                        ST_Distance(ss.location::geography, h.position::geography) AS distance_meters
                    FROM subway_station ss
                    CROSS JOIN house h
                    WHERE ST_DWithin(ss.location::geography, h.position::geography, :radiusMeters)
                
                    UNION ALL
                
                    SELECT
                        swt.id AS id,
                        swt.type AS type,
                        swt.store_name AS name,
                        swt.latitude AS latitude,
                        swt.longitude AS longitude,
                        ST_Distance(swt.location::geography, h.position::geography) AS distance_meters
                    FROM store_with_type swt
                    CROSS JOIN house h
                    WHERE swt.type IS NOT NULL
                      AND ST_DWithin(swt.location::geography, h.position::geography, :radiusMeters)
                )
                SELECT
                    ms.id,
                    ms.type,
                    ms.name,
                    ms.latitude,
                    ms.longitude
                FROM marker_source ms
                ORDER BY ms.distance_meters ASC, ms.id ASC
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(markerSql)
                .setParameter("houseId", houseId)
                .setParameter("radiusMeters", radiusMeters)
                .getResultList();

        return rows.stream()
                .map(this::toMarkerRow)
                .toList();
    }

    @Override
    public RegionStoreInfrastructureCount findStoreCountsBySigunguCode(String sigunguCode) {
        // store.region_code(10자리) 앞 5자리를 sigunguCode와 매칭해 시군구 범위로 집계
        String sql = """
                WITH store_with_type AS (
                    SELECT
                        CASE
                            WHEN s.category_code = 'G20405' THEN 'CONVENIENCE'
                            WHEN s.category_code = 'S20901' THEN 'LAUNDRY'
                            WHEN s.category_code = 'I21201' THEN 'CAFE'
                            WHEN s.category_code IN ('G21501', 'G21502') THEN 'PHARMACY'
                            WHEN slc.code = 'Q1' THEN 'HOSPITAL'
                            ELSE NULL
                        END AS type
                    FROM store s
                    JOIN store_small_category ssc ON s.category_code = ssc.code
                    JOIN store_medium_category smc ON ssc.medium_category_code = smc.code
                    JOIN store_large_category slc ON smc.large_category_code = slc.code
                    WHERE LEFT(s.region_code, 5) = :sigunguCode
                )
                SELECT
                    CAST(COALESCE(SUM(CASE WHEN swt.type = 'CONVENIENCE' THEN 1 ELSE 0 END), 0) AS INTEGER) AS convenience_store_count,
                    CAST(COALESCE(SUM(CASE WHEN swt.type = 'CAFE' THEN 1 ELSE 0 END), 0) AS INTEGER) AS cafe_count,
                    CAST(COALESCE(SUM(CASE WHEN swt.type = 'HOSPITAL' THEN 1 ELSE 0 END), 0) AS INTEGER) AS hospital_count,
                    CAST(COALESCE(SUM(CASE WHEN swt.type = 'LAUNDRY' THEN 1 ELSE 0 END), 0) AS INTEGER) AS laundry_count
                FROM store_with_type swt
                """;

        // 집계 결과는 항상 1행이며 null 값은 toInteger에서 0으로 보정
        Object[] row = (Object[]) entityManager.createNativeQuery(sql)
                .setParameter("sigunguCode", sigunguCode)
                .getSingleResult();

        return RegionStoreInfrastructureCount.builder()
                .convenienceStoreCount(toInteger(row[0]))
                .cafeCount(toInteger(row[1]))
                .hospitalCount(toInteger(row[2]))
                .laundryCount(toInteger(row[3]))
                .build();
    }

    private InfrastructureSummaryRow toSummaryRow(Object[] row) {
        return InfrastructureSummaryRow.builder()
                .type(toStringValue(row[0]))
                .count(toInteger(row[1]))
                .nearestDistanceMeters(toNullableInteger(row[2]))
                .build();
    }

    private InfrastructureMarkerRow toMarkerRow(Object[] row) {
        return InfrastructureMarkerRow.builder()
                .id(toLong(row[0]))
                .type(toStringValue(row[1]))
                .name(toStringValue(row[2]))
                .latitude(toDouble(row[3]))
                .longitude(toDouble(row[4]))
                .build();
    }

    // 숫자 -> Integer 변환 (null은 0으로 변환)
    private Integer toInteger(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }

    // 숫자 -> nullable Integer 변환 (null은 유지)
    private Integer toNullableInteger(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    // 숫자 -> Long 변환
    private Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    // 숫자 -> Double 변환
    private Double toDouble(Object value) {
        return value == null ? null : ((Number) value).doubleValue();
    }

    // 객체 -> 문자열 변환    
    private String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }
}
