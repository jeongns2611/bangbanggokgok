package com.ssafy.backend.domain.recommendation.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.backend.domain.code.entity.QCommonCodeDetail;
import com.ssafy.backend.domain.house.entity.QCurrentHouse;
import com.ssafy.backend.domain.house.entity.QHouseFloor;
import com.ssafy.backend.domain.house.entity.QHouseImage;
import com.ssafy.backend.domain.recommendation.dto.response.*;
import com.ssafy.backend.domain.region.entity.QRegion;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RecommendationRepositoryImpl implements RecommendationRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    /**
     * 지역 코드로 비교 목록 화면에 필요한 매물 요약 정보를 조회한다.
     * 이미지처럼 1:N 데이터는 제외하고, 매물 기본 속성만 projection으로 반환한다.
     * <p>
     * 예외:
     * - 이 메서드에서 비즈니스 예외를 직접 던지지 않는다.
     * - 잘못된 지역명 검증은 상위 서비스/RegionService에서 처리한다.
     */
    @Override
    public List<RecommendationHouseListSummary> findHouseSummariesByRegion(String sidoCode, String sigunguCode) {
        QCurrentHouse currentHouse = QCurrentHouse.currentHouse;
        QRegion region = QRegion.region;
        QHouseFloor houseFloor = QHouseFloor.houseFloor;
        QCommonCodeDetail houseTypeCode = new QCommonCodeDetail("houseTypeCode");
        QCommonCodeDetail rentTypeCode = new QCommonCodeDetail("rentTypeCode");
        QCommonCodeDetail houseStatusCode = new QCommonCodeDetail("houseStatusCode");
        QCommonCodeDetail floorCode = new QCommonCodeDetail("floorCode");

        return queryFactory
                .select(Projections.constructor(
                        RecommendationHouseListSummary.class,
                        currentHouse.id,
                        Expressions.stringTemplate(
                                "concat({0}, ' ', {1}, ' ', {2})",
                                region.regionSido.sidoName,
                                region.regionSigungu.sigunguName,
                                region.dongName
                        ),
                        houseTypeCode.codeName,
                        rentTypeCode.codeName,
                        houseStatusCode.codeName,
                        currentHouse.deposit,
                        currentHouse.monthlyCost,
                        currentHouse.managementCost,
                        currentHouse.managementItems.coalesce(""),
                        currentHouse.floorSize,
                        floorCode.codeName.coalesce("")
                ))
                .from(currentHouse)
                .join(region).on(currentHouse.regionCode.eq(region.regionCode))
                .join(houseTypeCode).on(currentHouse.houseTypeCode.eq(houseTypeCode.id.longValue()))
                .join(rentTypeCode).on(currentHouse.rentTypeCode.eq(rentTypeCode.id.longValue()))
                .join(houseStatusCode).on(currentHouse.houseStatusCode.eq(houseStatusCode.id.longValue()))
                .leftJoin(houseFloor).on(houseFloor.currentHouse.id.eq(currentHouse.id))
                .leftJoin(floorCode).on(houseFloor.houseFloorCode.id.eq(floorCode.id))
                .where(
                        currentHouse.sidoCode.eq(sidoCode),
                        currentHouse.sigunguCode.eq(sigunguCode),
                        currentHouse.deletedAt.isNull(),
                        currentHouse.soldAt.isNull()
                )
                .orderBy(currentHouse.id.desc())
                .fetch();
    }

    /**
     * 목록에 포함된 여러 매물의 이미지 정보를 한 번에 조회한다.
     * 썸네일이 먼저 오도록 정렬해서 서비스 계층이 그대로 응답으로 조립할 수 있게 한다.
     * <p>
     * 예외:
     * - 이 메서드에서 비즈니스 예외를 직접 던지지 않는다.
     * - houseIds가 비어 있으면 빈 리스트를 반환한다.
     */
    @Override
    public List<RecommendationHouseImageRow> findHouseImagesByHouseIds(List<Long> houseIds) {
        if (houseIds == null || houseIds.isEmpty()) {
            return Collections.emptyList();
        }

        QHouseImage houseImage = QHouseImage.houseImage;

        return queryFactory
                .select(Projections.constructor(
                        RecommendationHouseImageRow.class,
                        houseImage.currentHouse.id,
                        houseImage.imageUrl,
                        houseImage.isThumbnail
                ))
                .from(houseImage)
                .where(houseImage.currentHouse.id.in(houseIds))
                .orderBy(
                        houseImage.currentHouse.id.asc(),
                        houseImage.isThumbnail.desc(),
                        houseImage.id.asc()
                )
                .fetch();
    }

    /**
     * 기능 1-4. 지역구 실매물 상세 조회에 사용됩니다.
     * @param houseId 를 받아 관련 상세 정보를 조회합니다.
     * @return RecommendationHouseDetailSummary
     */
    /**
     * 단일 매물 상세 화면의 기본 정보를 조회한다.
     * 주소/코드명/층 정보와 함께 후속 계산에 필요한 위치 좌표를 포함한다.
     * <p>
     * 예외:
     * - 이 메서드에서 비즈니스 예외를 직접 던지지 않는다.
     * - 조회 결과가 없으면 Optional.empty()를 반환하고, HOUSE_NOT_FOUND 처리는 상위 서비스가 담당한다.
     */
    @Override
    public Optional<RecommendationHouseDetailSummary> findHouseDetailById(Long houseId) {
        QCurrentHouse currentHouse = QCurrentHouse.currentHouse;
        QRegion region = QRegion.region;
        QHouseFloor houseFloor = QHouseFloor.houseFloor;
        QCommonCodeDetail houseTypeCode = new QCommonCodeDetail("detailHouseTypeCode");
        QCommonCodeDetail rentTypeCode = new QCommonCodeDetail("detailRentTypeCode");
        QCommonCodeDetail houseStatusCode = new QCommonCodeDetail("detailHouseStatusCode");
        QCommonCodeDetail floorCode = new QCommonCodeDetail("detailFloorCode");

        RecommendationHouseDetailSummary summary = queryFactory
                .select(Projections.constructor(
                        RecommendationHouseDetailSummary.class,
                        currentHouse.id,
                        Expressions.stringTemplate(
                                "concat({0}, ' ', {1}, ' ', {2})",
                                region.regionSido.sidoName,
                                region.regionSigungu.sigunguName,
                                region.dongName
                        ),
                        houseTypeCode.codeName,
                        rentTypeCode.codeName,
                        houseStatusCode.codeName,
                        currentHouse.deposit,
                        currentHouse.monthlyCost,
                        currentHouse.managementCost,
                        currentHouse.managementItems.coalesce(""),
                        currentHouse.floorSize,
                        currentHouse.buildYear.coalesce(0),
                        currentHouse.description.coalesce(""),
                        currentHouse.viewCount,
                        floorCode.codeName.coalesce(""),
                        currentHouse.sigunguCode,
                        currentHouse.position
                ))
                .from(currentHouse)
                .join(region).on(currentHouse.regionCode.eq(region.regionCode))
                .join(houseTypeCode).on(currentHouse.houseTypeCode.eq(houseTypeCode.id.longValue()))
                .join(rentTypeCode).on(currentHouse.rentTypeCode.eq(rentTypeCode.id.longValue()))
                .join(houseStatusCode).on(currentHouse.houseStatusCode.eq(houseStatusCode.id.longValue()))
                .leftJoin(houseFloor).on(houseFloor.currentHouse.id.eq(currentHouse.id))
                .leftJoin(floorCode).on(houseFloor.houseFloorCode.id.eq(floorCode.id))
                .where(
                        currentHouse.id.eq(houseId),
                        currentHouse.deletedAt.isNull()
                )
                .fetchOne();

        return Optional.ofNullable(summary);
    }

    /**
     * 단일 매물 상세 화면에 노출할 이미지 목록을 조회한다.
     * <p>
     * 예외:
     * - 이 메서드에서 비즈니스 예외를 직접 던지지 않는다.
     * - 이미지가 없으면 빈 리스트를 반환한다.
     */
    @Override
    public List<RecommendationHouseImageRow> findHouseImagesByHouseId(Long houseId) {
        QHouseImage houseImage = QHouseImage.houseImage;

        return queryFactory
                .select(Projections.constructor(
                        RecommendationHouseImageRow.class,
                        houseImage.currentHouse.id,
                        houseImage.imageUrl,
                        houseImage.isThumbnail
                ))
                .from(houseImage)
                .where(houseImage.currentHouse.id.eq(houseId))
                .orderBy(houseImage.isThumbnail.desc(), houseImage.id.asc())
                .fetch();
    }

    /**
     * 매물 주변 500m 내 생활 인프라 개수를 집계한다.
     * 상점 카테고리 표준 코드가 아직 확정되지 않아 현재는 카테고리명 문자열 매칭을 사용한다.
     * <p>
     * 예외:
     * - 이 메서드에서 비즈니스 예외를 직접 던지지 않는다.
     * - houseId는 상위 서비스에서 이미 검증되었다고 가정한다.
     */
    @Override
    public RecommendationHouseDetailResponse.InfraCount findInfraCountByHouseId(Long houseId) {
        /*
         * 가게 분류 체계가 코드 기반이라, 현재는 분류명 키워드로 시설 유형을 판별합니다.
         * 추후 시설 카테고리 표준 코드가 정리되면 문자열 contains 대신 코드 비교로 바꾸는 편이 안전합니다.
         */
        Object[] row = (Object[]) entityManager.createNativeQuery("""
                        SELECT
                            COALESCE((
                                SELECT COUNT(*)
                                FROM store s
                                JOIN store_small_category ssc ON s.category_code = ssc.code
                                JOIN store_medium_category smc ON ssc.medium_category_code = smc.code
                                JOIN store_large_category slc ON smc.large_category_code = slc.code
                                JOIN current_house ch2 ON ch2.id = :houseId
                                WHERE ST_DWithin(s.location::geography, ch2.position::geography, 500)
                                  AND (smc.name LIKE '%편의점%' OR slc.name LIKE '%편의점%')
                            ), 0) AS convenience_store_count,
                            COALESCE((
                                SELECT COUNT(*)
                                FROM store s
                                JOIN store_small_category ssc ON s.category_code = ssc.code
                                JOIN store_medium_category smc ON ssc.medium_category_code = smc.code
                                JOIN current_house ch2 ON ch2.id = :houseId
                                WHERE ST_DWithin(s.location::geography, ch2.position::geography, 500)
                                  AND smc.name LIKE '%세탁%'
                            ), 0) AS laundry_count,
                            COALESCE((
                                SELECT COUNT(*)
                                FROM store s
                                JOIN store_small_category ssc ON s.category_code = ssc.code
                                JOIN store_medium_category smc ON ssc.medium_category_code = smc.code
                                JOIN store_large_category slc ON smc.large_category_code = slc.code
                                JOIN current_house ch2 ON ch2.id = :houseId
                                WHERE ST_DWithin(s.location::geography, ch2.position::geography, 500)
                                  AND (smc.name LIKE '%카페%' OR slc.name LIKE '%카페%')
                            ), 0) AS cafe_count,
                            COALESCE((
                                SELECT COUNT(*)
                                FROM store s
                                JOIN store_small_category ssc ON s.category_code = ssc.code
                                JOIN store_medium_category smc ON ssc.medium_category_code = smc.code
                                JOIN store_large_category slc ON smc.large_category_code = slc.code
                                JOIN current_house ch2 ON ch2.id = :houseId
                                WHERE ST_DWithin(s.location::geography, ch2.position::geography, 500)
                                  AND (smc.name LIKE '%병원%' OR slc.name LIKE '%병원%')
                            ), 0) AS hospital_count,
                            COALESCE((
                                SELECT COUNT(*)
                                FROM store s
                                JOIN store_small_category ssc ON s.category_code = ssc.code
                                JOIN store_medium_category smc ON ssc.medium_category_code = smc.code
                                JOIN current_house ch2 ON ch2.id = :houseId
                                WHERE ST_DWithin(s.location::geography, ch2.position::geography, 500)
                                  AND smc.name LIKE '%약국%'
                            ), 0) AS pharmacy_count,
                            COALESCE((
                                SELECT COUNT(*)
                                FROM bus_stop bs
                                JOIN current_house ch2 ON ch2.id = :houseId
                                WHERE ST_DWithin(bs.location::geography, ch2.position::geography, 500)
                            ), 0) AS bus_stop_count
                        """)
                .setParameter("houseId", houseId)
                .getSingleResult();

        return RecommendationHouseDetailResponse.InfraCount.builder()
                .convenienceStoreCount(toInteger(row[0]))
                .laundryCount(toInteger(row[1]))
                .cafeCount(toInteger(row[2]))
                .hospitalCount(toInteger(row[3]))
                .pharmacyCount(toInteger(row[4]))
                .busStopCount(toInteger(row[5]))
                .build();
    }

    /**
     * 매물에서 각 생활 인프라까지의 최단 직선거리를 계산한다.
     * 반환 단위는 미터이며, 대상 시설이 없으면 0을 반환한다.
     * <p>
     * 예외:
     * - 이 메서드에서 비즈니스 예외를 직접 던지지 않는다.
     * - houseId는 상위 서비스에서 이미 검증되었다고 가정한다.
     */
    @Override
    public RecommendationHouseDetailResponse.MinDist findMinDistByHouseId(Long houseId) {
        Object[] row = (Object[]) entityManager.createNativeQuery("""
                        SELECT
                            COALESCE((
                                SELECT MIN(CAST(ST_DistanceSphere(s.location, ch2.position) AS INTEGER))
                                FROM store s
                                JOIN store_small_category ssc ON s.category_code = ssc.code
                                JOIN store_medium_category smc ON ssc.medium_category_code = smc.code
                                JOIN store_large_category slc ON smc.large_category_code = slc.code
                                JOIN current_house ch2 ON ch2.id = :houseId
                                WHERE smc.name LIKE '%편의점%' OR slc.name LIKE '%편의점%'
                            ), 0) AS convenience_dist,
                            COALESCE((
                                SELECT MIN(CAST(ST_DistanceSphere(s.location, ch2.position) AS INTEGER))
                                FROM store s
                                JOIN store_small_category ssc ON s.category_code = ssc.code
                                JOIN store_medium_category smc ON ssc.medium_category_code = smc.code
                                JOIN current_house ch2 ON ch2.id = :houseId
                                WHERE smc.name LIKE '%세탁%'
                            ), 0) AS laundry_dist,
                            COALESCE((
                                SELECT MIN(CAST(ST_DistanceSphere(s.location, ch2.position) AS INTEGER))
                                FROM store s
                                JOIN store_small_category ssc ON s.category_code = ssc.code
                                JOIN store_medium_category smc ON ssc.medium_category_code = smc.code
                                JOIN store_large_category slc ON smc.large_category_code = slc.code
                                JOIN current_house ch2 ON ch2.id = :houseId
                                WHERE smc.name LIKE '%카페%' OR slc.name LIKE '%카페%'
                            ), 0) AS cafe_dist,
                            COALESCE((
                                SELECT MIN(CAST(ST_DistanceSphere(s.location, ch2.position) AS INTEGER))
                                FROM store s
                                JOIN store_small_category ssc ON s.category_code = ssc.code
                                JOIN store_medium_category smc ON ssc.medium_category_code = smc.code
                                JOIN store_large_category slc ON smc.large_category_code = slc.code
                                JOIN current_house ch2 ON ch2.id = :houseId
                                WHERE smc.name LIKE '%병원%' OR slc.name LIKE '%병원%'
                            ), 0) AS hospital_dist,
                            COALESCE((
                                SELECT MIN(CAST(ST_DistanceSphere(s.location, ch2.position) AS INTEGER))
                                FROM store s
                                JOIN store_small_category ssc ON s.category_code = ssc.code
                                JOIN store_medium_category smc ON ssc.medium_category_code = smc.code
                                JOIN current_house ch2 ON ch2.id = :houseId
                                WHERE smc.name LIKE '%약국%'
                            ), 0) AS pharmacy_dist,
                            COALESCE((
                                SELECT MIN(CAST(ST_DistanceSphere(ss.location, ch2.position) AS INTEGER))
                                FROM subway_station ss
                                JOIN current_house ch2 ON ch2.id = :houseId
                            ), 0) AS subway_dist
                        """)
                .setParameter("houseId", houseId)
                .getSingleResult();

        return RecommendationHouseDetailResponse.MinDist.builder()
                .convenienceDist(toInteger(row[0]))
                .laundryDist(toInteger(row[1]))
                .cafeDist(toInteger(row[2]))
                .hospitalDist(toInteger(row[3]))
                .pharmacyDist(toInteger(row[4]))
                .subwayDist(toInteger(row[5]))
                .build();
    }

    /**
     * 행정구역 단위 통계 정보를 조회한다.
     * 현재 스키마에 안전 관련 원천 테이블이 없어 식비 지표만 실제 데이터로 채우고, 나머지는 0으로 보정한다.
     * <p>
     * 예외:
     * - 이 메서드에서 비즈니스 예외를 직접 던지지 않는다.
     * - houseId 미검증 상태에서 호출하면 JPA 조회 예외가 날 수 있으므로 상위 서비스 선검증을 전제한다.
     */
    @Override
    public RecommendationHouseDetailResponse.DongStats findDongStatsByHouseId(Long houseId) {
        /*
         * 현재 코드베이스/스키마에는 CCTV, 가로등, 안전시설, 안전점수 원천 테이블이 없습니다.
         * 그래서 상세 API는 region_statistic에서 확인 가능한 식비 지표만 채우고,
         * 나머지 안전 관련 수치는 0으로 반환합니다.
         */
        Object[] row = (Object[]) entityManager.createNativeQuery("""
                        SELECT
                            0 AS cctv_count,
                            0 AS street_light_count,
                            0 AS safety_facility_count,
                            0.0 AS safety_score,
                            COALESCE(rs.pork_belly_cost, 0) AS avg_meat_price,
                            COALESCE(rs.kimbap_cost, 0) AS avg_meal_price
                        FROM current_house ch
                        LEFT JOIN region_statistic rs ON ch.sigungu_code = rs.sigungu_code
                        WHERE ch.id = :houseId
                        """)
                .setParameter("houseId", houseId)
                .getSingleResult();

        return RecommendationHouseDetailResponse.DongStats.builder()
                .cctvCount(toInteger(row[0]))
                .streetLightCount(toInteger(row[1]))
                .safetyFacilityCount(toInteger(row[2]))
                .safetyScore(toDouble(row[3]))
                .avgMeatPrice(toInteger(row[4]))
                .avgMealPrice(toInteger(row[5]))
                .build();
    }

    /**
     * Top10 추천 점수 계산을 위해 전체 활성 매물을 조회한다.
     * <p>
     * [조회 대상]
     * - deletedAt IS NULL: 논리삭제 안 된 매물
     * - soldAt IS NULL: 아직 거래되지 않은 매물
     * → 즉, 현재 "거래가능"한 전체 매물이 대상
     * <p>
     * [조인 구조] (findHouseSummariesByRegion과 동일한 패턴)
     * current_house
     * ├─ INNER JOIN region                (지역 코드 → 시도/시군구/동 이름)
     * ├─ INNER JOIN common_code_detail ×3 (주거형태/임대유형/매물상태 코드 → 코드명)
     * ├─ LEFT JOIN house_floor            (층 정보, 없을 수 있음)
     * └─ LEFT JOIN common_code_detail     (층 코드 → 코드명)
     * <p>
     * [반환]
     * RecommendationTop10HouseSummary 리스트 (position 좌표 포함)
     * → 서비스 계층에서 이 리스트를 순회하며 점수를 계산한다.
     * <p>
     * ⚠ 주의:
     * - 이름과 달리 "Top 10"만 가져오는 것이 아니라 전체 활성 매물을 반환한다.
     * 정렬과 Top10 슬라이싱은 서비스 계층(buildRankedRecommendations)에서 수행.
     * - 매물 수가 많으면 전체 로드 + ODsay 호출로 첫 요청 latency가 커질 수 있다.
     */
    @Override
    public List<RecommendationTop10HouseSummary> findTop10HouseSummaries(String sidoName, String sigunguName) {
        QCurrentHouse currentHouse = QCurrentHouse.currentHouse;
        QRegion region = QRegion.region;
        QHouseFloor houseFloor = QHouseFloor.houseFloor;
        // Q타입 alias를 다르게 줘서 findHouseSummariesByRegion()의 alias와 충돌 방지
        QCommonCodeDetail houseTypeCode = new QCommonCodeDetail("top10HouseTypeCode");
        QCommonCodeDetail rentTypeCode = new QCommonCodeDetail("top10RentTypeCode");
        QCommonCodeDetail houseStatusCode = new QCommonCodeDetail("top10HouseStatusCode");
        QCommonCodeDetail floorCode = new QCommonCodeDetail("top10FloorCode");

        return queryFactory
                .select(Projections.constructor(
                        RecommendationTop10HouseSummary.class,
                        currentHouse.id,                          // houseId
                        Expressions.stringTemplate(               // dong (주소 문자열 조합)
                                "concat(trim({0}), ' ', trim({1}), ' ', trim({2}))",
                                region.regionSido.sidoName,
                                region.regionSigungu.sigunguName,
                                region.dongName
                        ),
                        houseTypeCode.codeName,                   // houseType (주거형태)
                        rentTypeCode.codeName,                    // rentType (임대유형)
                        houseStatusCode.codeName,                 // houseStatus (매물상태)
                        currentHouse.deposit,                     // 보증금
                        currentHouse.monthlyCost,                 // 월세
                        currentHouse.managementCost,              // 관리비
                        currentHouse.managementItems.coalesce(""),// 관리비 포함 항목
                        currentHouse.floorSize,                   // 전용면적
                        floorCode.codeName.coalesce(""),          // 층
                        currentHouse.position                     // 매물 좌표 (통근 계산 + 지도 표시용)
                ))
                .from(currentHouse)
                .join(region).on(currentHouse.regionCode.eq(region.regionCode))
                .join(houseTypeCode).on(currentHouse.houseTypeCode.eq(houseTypeCode.id.longValue()))
                .join(rentTypeCode).on(currentHouse.rentTypeCode.eq(rentTypeCode.id.longValue()))
                .join(houseStatusCode).on(currentHouse.houseStatusCode.eq(houseStatusCode.id.longValue()))
                .leftJoin(houseFloor).on(houseFloor.currentHouse.id.eq(currentHouse.id))
                .leftJoin(floorCode).on(houseFloor.houseFloorCode.id.eq(floorCode.id))
                .where(
                        // [지역 필터링] DB 데이터의 끝 공백 예외 처리를 위해 trim() 적용
                        region.regionSido.sidoName.trim().eq(sidoName.trim()),
                        region.regionSigungu.sigunguName.trim().eq(sigunguName.trim()),
                        currentHouse.deletedAt.isNull(),   // 논리삭제 안 된 매물만
                        currentHouse.soldAt.isNull()       // 미거래 매물만
                )
                .fetch();
    }

    /**
     * native query 결과를 Integer로 안전하게 변환한다.
     * null은 0으로 보정한다.
     */
    private Integer toInteger(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }

    /**
     * native query 결과를 Double로 안전하게 변환한다.
     * null은 0.0으로 보정한다.
     */
    private Double toDouble(Object value) {
        return value == null ? 0.0 : ((Number) value).doubleValue();
    }
}
