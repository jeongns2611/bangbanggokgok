package com.ssafy.backend.domain.recommendation.service;


import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ssafy.backend.domain.commute.dto.request.CommuteRequest;
import com.ssafy.backend.domain.commute.dto.response.OdsayResponse;
import com.ssafy.backend.domain.commute.service.OdsayTransitService;
import com.ssafy.backend.domain.infrastructure.dto.response.NearbyInfrastructureResponse;
import com.ssafy.backend.domain.infrastructure.service.InfrastructureService;
import com.ssafy.backend.domain.infrastructure.type.InfrastructureType;
import com.ssafy.backend.domain.commute.service.CommuteMetricsService;
import com.ssafy.backend.domain.commute.service.CommuteMetricsService.CommuteMetrics;
import com.ssafy.backend.domain.recommendation.dto.request.RecommendationHouseListRequest;
import com.ssafy.backend.domain.recommendation.dto.request.RecommendationRegionTopRequest;
import com.ssafy.backend.domain.recommendation.dto.request.RecommendationTop10Request;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationHouseDetailResponse;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationHouseDetailSummary;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationHouseImageRow;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationHouseListResponse;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationHouseListSummary;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationRegionTopResponse;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationTop10Response;
import com.ssafy.backend.domain.recommendation.repository.RecommendationRepository;
import com.ssafy.backend.domain.region.service.RegionService;
import com.ssafy.backend.domain.statistics.service.DongStatsComposer;
import com.ssafy.backend.domain.user.entity.UserNeed;
import com.ssafy.backend.domain.user.repository.UserNeedRepository;
import com.ssafy.backend.global.auth.principal.CustomOAuth2User;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 추천 도메인 퍼사드(Facade) 서비스.
 *
 * 컨트롤러가 호출하는 유일한 진입점. 실제 비즈니스 로직은 하위 서비스에 위임한다:
 *   - 기능 ① getComparisonData : 지역별 실매물 목록 조회 (이 클래스에서 직접 처리)
 *   - 기능 ② getHouseDetail    : 매물 상세 조회 (CommuteMetricsService에 통근 정보 위임)
 *   - 기능 ③ getTop10Recommendations : Top10 추천 (RecommendationTop10Service에 위임)
 *
 * ──────────────────────────────────────────────────────────────
 * [Top10 추천 전체 흐름 요약]
 *
 *   (1) 프론트: GET /api/v1/recommendations/houses/top10?id={userNeedId}&houseId={lastHouseId}
 *   (2) 인증 사용자 ID 추출 → UserNeed(매물 조건) 조회
 *   (3) Redis 캐시 hit  → 저장된 순위 리스트 사용
 *       Redis 캐시 miss → 전체 매물 점수 계산 → 정렬 → Redis 저장 (TTL 1시간)
 *   (4) lastHouseId 이후 10건을 슬라이싱 → hasNext/nextHouseId 포함하여 응답
 *
 * ──────────────────────────────────────────────────────────────
 * [점수 산정 가중치 — HouseScoreCalculator 참조]
 *
 *   항목               | 만점  | 우선순위
 *   ─────────────────  | ───── | ──────
 *   보증금(deposit)    | 20점  | 1순위
 *   월세(monthlyCost)  | 20점  | 1순위
 *   주거형태(houseType)| 25점  | 2순위
 *   통근시간(commute)  | 15점  | 3순위
 *   전용면적(floorSize)| 10점  | 4순위
 *   임대유형(rentType) | 10점  | (보조)
 *   합계               |100점  |
 *
 * ──────────────────────────────────────────────────────────────
 * [commuteDistance 정책]
 *
 *   - Top10 추천 목록: commuteDistance 미포함 (순위 정렬에 불필요)
 *   - 매물 상세 조회:  commuteDistance 포함 (CommuteMetrics 통해 제공)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final RegionService regionService;
    private final UserNeedRepository userNeedRepository;
    private final OdsayTransitService odsayTransitService;
    private final InfrastructureService infrastructureService;

    /** 통근 지표 서비스 (ODsay API + DB 캐시) */
    private final CommuteMetricsService commuteMetricsService;

    /** Top10 추천 서비스 (Redis 캐시 + 점수 정렬 + 페이징) */
    private final RecommendationTop10Service recommendationTop10Service;
    private final RecommendationRegionTopService recommendationRegionTopService;
    private final DongStatsComposer dongStatsComposer;

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  기능 ①  지역별 매물 목록 조회 (기존 비교 화면용)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 지역별 실매물 데이터를 목록 형태로 제공합니다.
     *
     * 이미지가 1:N 구조라 house_image까지 조인하면 매물 row가 이미지 수만큼 반복됩니다.
     * 그래서 매물 summary 조회와 이미지 조회를 분리한 뒤 최종 응답으로 조립합니다.
     *
     * @param request 시도명 + 시군구명 (예: "서울특별시", "강남구")
     * @return 해당 지역 매물 목록 (없으면 빈 리스트)
     */
    public RecommendationHouseListResponse getComparisonData(RecommendationHouseListRequest request) {
        // 지역명이 유효하지 않으면 RegionService에서 REGION_NOT_FOUND 예외를 던진다.
        String sidoCode = regionService.getSidoCode(request.getSidoName());
        String sigunguCode = regionService.getSigunguCode(request.getSidoName(), request.getSigunguName());

        // 지역 코드 기준으로 매물 핵심 정보 조회
        List<RecommendationHouseListSummary> summaries =
                recommendationRepository.findHouseSummariesByRegion(sidoCode, sigunguCode);

        // 유효한 지역인데 매물이 없는 경우 → 빈 리스트 반환
        if (summaries.isEmpty()) {
            return RecommendationHouseListResponse.builder()
                    .comparisonData(Collections.emptyList())
                    .build();
        }

        // 이미지 조회에 사용할 매물 ID 목록 추출
        List<Long> houseIds = summaries.stream()
                .map(RecommendationHouseListSummary::getHouseId)
                .toList();

        // 이미지 row를 매물별로 그룹핑 → houseId별 List<ImageInfo> Map 구성
        Map<Long, List<RecommendationHouseListResponse.ImageInfo>> imagesByHouseId =
                recommendationRepository.findHouseImagesByHouseIds(houseIds).stream()
                        .collect(Collectors.groupingBy(
                                RecommendationHouseImageRow::getHouseId,
                                Collectors.mapping(
                                        image -> RecommendationHouseListResponse.ImageInfo.builder()
                                                .imageUrl(image.getImageUrl())
                                                .isThumbnail(image.getIsThumbnail())
                                                .build(),
                                        Collectors.toList()
                                )
                        ));

        // 매물 정보 + 이미지 목록을 합쳐 최종 응답 조립
        List<RecommendationHouseListResponse.ComparisonData> comparisonData = summaries.stream()
                .map(summary -> RecommendationHouseListResponse.ComparisonData.builder()
                        .houseId(summary.getHouseId())
                        .dong(summary.getDong())
                        .houseType(summary.getHouseType())
                        .rentType(summary.getRentType())
                        .houseStatus(summary.getHouseStatus())
                        .deposit(summary.getDeposit())
                        .monthlyCost(summary.getMonthlyCost())
                        .managementCost(summary.getManagementCost())
                        .managementItems(summary.getManagementItems())
                        .floorSize(summary.getFloorSize())
                        .floor(summary.getFloor())
                        .images(imagesByHouseId.getOrDefault(summary.getHouseId(), Collections.emptyList()))
                        .build())
                .toList();

        return RecommendationHouseListResponse.builder()
                .comparisonData(comparisonData)
                .build();
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  기능 ②  단일 매물 상세 조회
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 선택한 단일 매물의 상세 정보를 반환합니다.
     * 매물 기본 정보, 이미지, 통근 데이터, 주변 인프라, 동 단위 통계를 조립합니다.
     *
     * [조립 순서]
     *   1) 매물 기본 정보 (summary) — QueryDSL 쿼리
     *   2) 이미지 목록 — 별도 쿼리 (1:N 분리)
     *   3) 통근 데이터 — CommuteMetricsService (commuteTime + commuteDistance 포함)
     *   4) 주변 인프라 수 — 반경 500m Native Query
     *   5) 인프라 최단거리 — ST_DistanceSphere
     *   6) 동 단위 통계 — region_statistic 테이블
     *
     * @param houseId 매물 PK (CurrentHouse.id)
     * @return 상세 응답 (통근 시간 + 통근 거리 포함)
     */
    public RecommendationHouseDetailResponse getHouseDetail(Long houseId) {
        // 1) 매물 기본 정보
        RecommendationHouseDetailSummary summary = recommendationRepository.findHouseDetailById(houseId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.HOUSE_NOT_FOUND,
                        "존재하지 않는 매물입니다. houseId=" + houseId
                ));

        // 2) 이미지 목록 (1:N 별도 조회)
        List<RecommendationHouseDetailResponse.ImageInfo> images =
                recommendationRepository.findHouseImagesByHouseId(houseId).stream()
                        .map(image -> RecommendationHouseDetailResponse.ImageInfo.builder()
                                .imageUrl(image.getImageUrl())
                                .isThumbnail(image.getIsThumbnail())
                                .build())
                        .toList();

        // 3) 통근 데이터 — commuteTime + commuteDistance 모두 제공
        RecommendationHouseDetailResponse.CommuteData commuteData =
                buildCommuteDataSafely(summary.getPosition());

        NearbyInfrastructureResponse nearbyInfrastructure =
                infrastructureService.getNearbyInfrastructures(houseId);

        // 4) 주변 인프라 수
        RecommendationHouseDetailResponse.InfraCount infraCount =
                buildInfraCountFromNearby(nearbyInfrastructure);

        // 5) 인프라 최단거리
        RecommendationHouseDetailResponse.MinDist minDist =
                buildMinDistFromNearby(nearbyInfrastructure);

        // 6) 동 단위 통계
        RecommendationHouseDetailResponse.DongStats dongStats =
                dongStatsComposer.composeForRecommendation(summary.getSigunguCode());

        return RecommendationHouseDetailResponse.builder()
                .images(images)
                .houseId(summary.getHouseId())
                .dong(summary.getDong())
                .houseType(summary.getHouseType())
                .rentType(summary.getRentType())
                .houseStatus(summary.getHouseStatus())
                .deposit(summary.getDeposit())
                .monthlyCost(summary.getMonthlyCost())
                .managementCost(summary.getManagementCost())
                .managementItems(summary.getManagementItems())
                .floorSize(summary.getFloorSize())
                .buildYear(summary.getBuildYear())
                .description(summary.getDescription())
                .viewCount(summary.getViewCount())
                .floor(summary.getFloor())
                .latitude(summary.getPosition() != null ? summary.getPosition().getY() : null)
                .longitude(summary.getPosition() != null ? summary.getPosition().getX() : null)
                .commuteData(commuteData)
                .infraCount(infraCount)
                .minDist(minDist)
                .dongStats(dongStats)
                .build();
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  기능 ③  Top10 추천 매물 조회 → RecommendationTop10Service에 위임
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 사용자 니즈 기반 Top10 추천 매물을 페이징하여 반환한다.
     *
     * 실제 처리는 RecommendationTop10Service에 위임하며, 이 메서드는
     * 인증 사용자 확인 및 UserNeed 조회만 담당한다.
     *
     * @param request id(userNeedId) + houseId(마지막 본 매물 ID, 첫 조회 시 0)
     * @return 10건 단위 추천 매물 + 페이징 메타
     */
    public RecommendationTop10Response getTop10Recommendations(RecommendationTop10Request request) {
        // 인증 사용자 확인 
        // TODO 어노테이션으로 처리가 안되던가?
        Long currentUserId = getCurrentUserId();

        // 해당 사용자 소유의 UserNeed 조회 (논리삭제 미적용 항목만)
        UserNeed userNeed = userNeedRepository
                .findByIdAndUser_IdAndDeletedAtIsNull(request.getId(), currentUserId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "매물추천 조건을 찾을 수 없습니다. id=" + request.getId()
                ));

        // Top10 로직은 RecommendationTop10Service에 위임
        return recommendationTop10Service.getTop10(userNeed, request.getHouseId());
    }

    /**
     * 지역구 TOP3 추천/통계 조회
     */
    public RecommendationRegionTopResponse getTopRegionRecommendations(RecommendationRegionTopRequest request) {
        getCurrentUserId();
        return recommendationRegionTopService.getTopRegionRecommendations(request);
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  private 내부 메서드
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * [매물 상세용] 통근 데이터를 구성한다.
     *
     * 로그인 사용자의 "기본(isDefaultNeed=true)" UserNeed에서 직장 위치(targetPos)를 가져와
     * 매물 좌표 ↔ 직장 좌표 간 대중교통 시간·거리를 계산한다.
     * commuteTime과 commuteDistance를 모두 포함하여 반환한다.
     *
     * @param housePosition 매물 위치 좌표 (longitude=X, latitude=Y)
     * @return CommuteData (commuteTime + commuteDistance)
     */
    private RecommendationHouseDetailResponse.CommuteData buildCommuteData(Point housePosition) {
        UserNeed defaultUserNeed = userNeedRepository
                .findByUser_IdAndIsDefaultNeedTrueAndDeletedAtIsNull(getCurrentUserId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "기본 사용자 조건을 찾을 수 없습니다."
                ));

        Point targetPos = defaultUserNeed.getTargetPos();
        if (housePosition == null || targetPos == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "통근 정보를 계산할 좌표가 없습니다."
            );
        }

        // CommuteMetricsService에 통근 지표 조회 위임 (commuteTime + commuteDistance 포함)
        CommuteMetrics metrics = commuteMetricsService.getOrCreateForDetail(housePosition, targetPos);

        return RecommendationHouseDetailResponse.CommuteData.builder()
                .commuteTime(metrics.commuteTime())
                .commuteDistance(metrics.commuteDistance())
                .build();
    }

    /**
     * SecurityContext에서 현재 인증된 사용자의 ID를 추출한다.
     *
     * OAuth2 로그인 시: CustomOAuth2User.getUserId()
     * 테스트/기타: Authentication.getName()을 Long으로 파싱
     */

    private RecommendationHouseDetailResponse.CommuteData buildCommuteDataSafely(Point housePosition) {
        try {
            return buildCommuteData(housePosition);
        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.NOT_FOUND || e.getErrorCode() == ErrorCode.INVALID_INPUT) {
                return null;
            }
            throw e;
        }
    }

    private OdsayResponse.Path extractBestPath(OdsayResponse response) {
        if (response == null
                || response.getResult() == null
                || response.getResult().getPath() == null
                || response.getResult().getPath().isEmpty()) {
            throw new BusinessException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    "ODsay API에서 통근 경로를 찾지 못했습니다."
            );
        }

        return response.getResult().getPath().stream()
                .filter(path -> path.getInfo() != null)
                .min((path1, path2) -> Integer.compare(
                        path1.getInfo().getTotalTime(),
                        path2.getInfo().getTotalTime()
                ))
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.EXTERNAL_API_ERROR,
                        "ODsay API 응답에 경로 정보가 없습니다."
                ));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User.getUserId();
        }

        String name = authentication.getName();
        if (name != null && name.chars().allMatch(Character::isDigit)) {
            return Long.valueOf(name);
        }

        throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증 사용자 정보를 확인할 수 없습니다.");
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    // 인프라 요약 결과를 추천 상세의 InfraCount DTO로 변환
    private RecommendationHouseDetailResponse.InfraCount buildInfraCountFromNearby(
            NearbyInfrastructureResponse nearbyInfrastructure
    ) {
        Map<String, NearbyInfrastructureResponse.Summary> summaryByType = nearbyInfrastructure.getSummaries().stream()
                .collect(Collectors.toMap(
                        NearbyInfrastructureResponse.Summary::getType,
                        summary -> summary,
                        (left, right) -> left
                ));

        return RecommendationHouseDetailResponse.InfraCount.builder()
                .convenienceStoreCount(getCount(summaryByType, InfrastructureType.CONVENIENCE))
                .laundryCount(getCount(summaryByType, InfrastructureType.LAUNDRY))
                .cafeCount(getCount(summaryByType, InfrastructureType.CAFE))
                .hospitalCount(getCount(summaryByType, InfrastructureType.HOSPITAL))
                .pharmacyCount(getCount(summaryByType, InfrastructureType.PHARMACY))
                .busStopCount(getCount(summaryByType, InfrastructureType.BUS))
                .build();
    }

    // 인프라 요약 결과를 추천 상세의 MinDist DTO로 변환 (null 거리는 0)
    private RecommendationHouseDetailResponse.MinDist buildMinDistFromNearby(
            NearbyInfrastructureResponse nearbyInfrastructure
    ) {
        Map<String, NearbyInfrastructureResponse.Summary> summaryByType = nearbyInfrastructure.getSummaries().stream()
                .collect(Collectors.toMap(
                        NearbyInfrastructureResponse.Summary::getType,
                        summary -> summary,
                        (left, right) -> left
                ));

        return RecommendationHouseDetailResponse.MinDist.builder()
                .convenienceDist(getNearestDistance(summaryByType, InfrastructureType.CONVENIENCE))
                .laundryDist(getNearestDistance(summaryByType, InfrastructureType.LAUNDRY))
                .cafeDist(getNearestDistance(summaryByType, InfrastructureType.CAFE))
                .hospitalDist(getNearestDistance(summaryByType, InfrastructureType.HOSPITAL))
                .pharmacyDist(getNearestDistance(summaryByType, InfrastructureType.PHARMACY))
                .subwayDist(getNearestDistance(summaryByType, InfrastructureType.SUBWAY))
                .build();
    }

    private Integer getCount(
            Map<String, NearbyInfrastructureResponse.Summary> summaryByType,
            InfrastructureType type
    ) {
        NearbyInfrastructureResponse.Summary summary = summaryByType.get(type.getCode());
        return summary == null || summary.getCount() == null ? 0 : summary.getCount();
    }

    private Integer getNearestDistance(
            Map<String, NearbyInfrastructureResponse.Summary> summaryByType,
            InfrastructureType type
    ) {
        NearbyInfrastructureResponse.Summary summary = summaryByType.get(type.getCode());
        if (summary == null || summary.getNearestDistanceMeters() == null) {
            return 0;
        }
        return summary.getNearestDistanceMeters();
    }

}
