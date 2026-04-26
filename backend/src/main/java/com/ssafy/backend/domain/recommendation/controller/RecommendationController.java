package com.ssafy.backend.domain.recommendation.controller;

import com.ssafy.backend.domain.house.service.CurrentHouseService;
import com.ssafy.backend.domain.recommendation.dto.request.RecommendationHouseListRequest;
import com.ssafy.backend.domain.recommendation.dto.request.RecommendationRegionTopRequest;
import com.ssafy.backend.domain.recommendation.dto.request.RecommendationTop10Request;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationHouseDetailResponse;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationHouseListResponse;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationRegionTopResponse;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationTop10Response;
import com.ssafy.backend.domain.recommendation.service.RecommendationService;
import com.ssafy.backend.global.auth.principal.CustomOAuth2User;
import com.ssafy.backend.global.common.response.ApiResponse;
import com.ssafy.backend.global.error.code.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 추천 도메인 REST 컨트롤러.
 * <p>
 * 기본 URL: /api/v1/recommendations
 * <p>
 * 제공 API 목록:
 * ① GET /houses                   → 지역구 실매물 목록 조회 (비교 화면용)
 * ② GET /houses/{houseId}         → 단일 매물 상세 조회
 * ③ GET /houses/top10             → 사용자 니즈 기반 Top10 추천 (무한 스크롤)
 * <p>
 * 모든 API는 ApiResponse<T> 래퍼로 감싸 일관된 응답 형식을 유지한다.
 */
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final CurrentHouseService currentHouseService;

    /**
     * 기능 1-4. 지역구 실매물 목록 조회
     * <p>
     * 시도/시군구명을 쿼리 파라미터로 받아 해당 지역의 매물 목록을 반환한다.
     * 매물 이미지는 1:N 구조이므로 내부적으로 분리 조회 후 조립하여 응답한다.
     *
     * @param request sidoName(시도명), sigunguName(시군구명)
     */
    @GetMapping("/houses")
    public ApiResponse<RecommendationHouseListResponse> getComparisonData(
            @Valid @ModelAttribute RecommendationHouseListRequest request
    ) {
        return ApiResponse.success(
                SuccessCode.SUCCESS,
                recommendationService.getComparisonData(request)
        );
    }

    /**
     * 기능 1-4. 지역구 실매물 상세 조회
     * <p>
     * 매물 ID를 Path Variable로 받아 상세 정보를 반환한다.
     * 기본 정보 + 이미지 + 통근 데이터 + 인프라 정보 + 동 통계를 조립하여 응답한다.
     *
     * @param houseId 조회할 매물 ID (CurrentHouse PK)
     */
    @GetMapping("/houses/{houseId}")
    public ApiResponse<RecommendationHouseDetailResponse> getHouseDetail(
            @PathVariable Long houseId,
            @AuthenticationPrincipal CustomOAuth2User userDetails
    ) {
        RecommendationHouseDetailResponse response = recommendationService.getHouseDetail(houseId);
        Long userId = userDetails == null ? 1L : userDetails.getUserId();
        response.setIsLiked(currentHouseService.checkLiked(houseId, userId));

        return ApiResponse.success(SuccessCode.SUCCESS, response);
    }

    /**
     * Top10 추천 매물 조회 (무한 스크롤 지원)
     * <p>
     * [요청 파라미터]
     * - id      : UserNeed PK (매물 조건 ID) — 필수
     * - houseId : 마지막으로 본 매물 ID — 옵션 (기본값 0, 첫 조회 시)
     * <p>
     * [응답]
     * - recommendations : 10건 단위 추천 매물 리스트
     * - nextHouseId     : 다음 페이지 요청 시 보낼 houseId
     * - hasNext          : 다음 페이지 존재 여부 (false면 무한 스크롤 종료)
     * <p>
     * [내부 동작]
     * 1) SecurityContext에서 현재 사용자 확인
     * 2) 해당 사용자의 UserNeed 조회
     * 3) Redis 캐시에 순위 리스트가 있으면 재사용, 없으면 전체 매물 점수 계산 후 캐시
     * 4) houseId 기준 커서 위치에서 10건 슬라이싱 반환
     */
    @GetMapping("/houses/top10")
    public ApiResponse<RecommendationTop10Response> getTop10Recommendations(
            @Valid @ModelAttribute RecommendationTop10Request request,
            @AuthenticationPrincipal CustomOAuth2User userDetails
    ) {
        RecommendationTop10Response response = recommendationService.getTop10Recommendations(request);

        Long userId = userDetails == null ? 1L : userDetails.getUserId();
        List<Long> houseIds = response.getRecommendations().stream()
                .map(RecommendationTop10Response.RecommendationItem::getHouseId)
                .toList();

        Map<Long, Boolean> likedMap = currentHouseService.checkLikedList(userId, houseIds);
        response.getRecommendations().forEach(item ->
                item.setIsLiked(likedMap.getOrDefault(item.getHouseId(), false))
        );

        return ApiResponse.success(SuccessCode.SUCCESS, response);
    }

    /**
     * 지역구 TOP3 추천 및 통계 조회 API
     */
    @GetMapping("/regions/top")
    public ApiResponse<RecommendationRegionTopResponse> getTopRegionRecommendations(
            @Valid @ModelAttribute RecommendationRegionTopRequest request
    ) {
        return ApiResponse.success(
                SuccessCode.SUCCESS,
                recommendationService.getTopRegionRecommendations(request)
        );
    }
}
