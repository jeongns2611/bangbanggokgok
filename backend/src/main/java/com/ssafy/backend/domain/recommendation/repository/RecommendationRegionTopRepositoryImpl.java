package com.ssafy.backend.domain.recommendation.repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 지역구 TOP 추천 집계용 Repository
 */
@Repository
@RequiredArgsConstructor
public class RecommendationRegionTopRepositoryImpl implements RecommendationRegionTopRepository {

    private final EntityManager entityManager;

    @Override
    public List<RecommendationRegionTopHouseRow> findActiveHousesBySido(String sidoCode) {
        String sql = """
                SELECT
                    ch.id AS house_id,
                    ch.sigungu_code AS region_code,
                    h.code_name AS house_type,
                    r.code_name AS rent_type,
                    ch.deposit,
                    ch.monthly_cost,
                    ch.floor_size
                FROM current_house ch
                JOIN common_code_detail h ON ch.house_type_code = h.id
                JOIN common_code_detail r ON ch.rent_type_code = r.id
                WHERE ch.deleted_at IS NULL
                  AND ch.sold_at IS NULL
                  AND ch.sido_code = :sidoCode
                ORDER BY ch.sigungu_code ASC, ch.id ASC
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("sidoCode", sidoCode)
                .getResultList();

        return rows.stream()
                .map(row -> new RecommendationRegionTopHouseRow(
                        toLong(row[0]),
                        toStringValue(row[1]),
                        toStringValue(row[2]),
                        toStringValue(row[3]),
                        toInteger(row[4]),
                        toInteger(row[5]),
                        toDouble(row[6])
                ))
                .toList();
    }

    @Override
    public List<RecommendationRegionTopRow> findRegionStatics(String sidoCode) {
        String sql = """
                WITH store_with_type AS (
                    SELECT
                        LEFT(s.region_code, 5) AS sigungu_code,
                        CASE
                            WHEN s.category_code = 'G20405' THEN 'CONVENIENCE'
                            WHEN s.category_code = 'I21201' THEN 'CAFE'
                            WHEN slc.code = 'Q1' THEN 'HOSPITAL'
                            ELSE NULL
                        END AS type
                    FROM store s
                    JOIN store_small_category ssc ON s.category_code = ssc.code
                    JOIN store_medium_category smc ON ssc.medium_category_code = smc.code
                    JOIN store_large_category slc ON smc.large_category_code = slc.code
                    WHERE s.region_code IS NOT NULL
                ),
                facility AS (
                    SELECT
                        swt.sigungu_code,
                        CAST(COALESCE(SUM(CASE WHEN swt.type = 'CONVENIENCE' THEN 1 ELSE 0 END), 0) AS INTEGER) AS convenience_store_count,
                        CAST(COALESCE(SUM(CASE WHEN swt.type = 'CAFE' THEN 1 ELSE 0 END), 0) AS INTEGER) AS cafe_count,
                        CAST(COALESCE(SUM(CASE WHEN swt.type = 'HOSPITAL' THEN 1 ELSE 0 END), 0) AS INTEGER) AS hospital_count
                    FROM store_with_type swt
                    WHERE swt.type IS NOT NULL
                    GROUP BY swt.sigungu_code
                )
                SELECT
                    rs.sigungu_code AS region_code,
                    rs.sigungu_name,
                    COALESCE(fc.convenience_store_count, 0) AS convenience_store_count,
                    COALESCE(fc.cafe_count, 0) AS cafe_count,
                    COALESCE(fc.hospital_count, 0) AS hospital_count,
                    COALESCE(rst.kimbap_cost, 0) AS selected_region_food_cost
                FROM region_sigungu rs
                LEFT JOIN facility fc ON fc.sigungu_code = rs.sigungu_code
                LEFT JOIN region_statistic rst ON rst.sigungu_code = rs.sigungu_code
                WHERE rs.sido_code = :sidoCode
                ORDER BY rs.sigungu_code ASC
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("sidoCode", sidoCode)
                .getResultList();

        return rows.stream()
                .map(row -> new RecommendationRegionTopRow(
                        toStringValue(row[0]),
                        toStringValue(row[1]),
                        toInteger(row[2]),
                        toInteger(row[3]),
                        toInteger(row[4]),
                        toInteger(row[5])
                ))
                .toList();
    }

    private Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private Integer toInteger(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }

    private Double toDouble(Object value) {
        return value == null ? null : ((Number) value).doubleValue();
    }

    private String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }
}
