package com.ssafy.backend.domain.recommendation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Top10 추천 매물 API 최종 응답 DTO.
 * <p>
 * GET /api/v1/recommendations/houses/top10 의 응답 body.
 * <p>
 * [구조]
 * {
 * "recommendations": [ ... ],   ← 10건 단위 추천 매물 리스트 (RecommendationItem)
 * "nextHouseId": 501,           ← 다음 페이지 요청 시 보낼 커서 ID
 * "hasNext": true               ← false이면 무한 스크롤 종료
 * }
 * <p>
 * [페이징 동작]
 * - 첫 요청: houseId=0 → 1위~10위 반환, nextHouseId=10위 매물 ID
 * - 2번째:   houseId=nextHouseId → 11위~20위 반환
 * - 마지막:  남은 매물 < 10개 → hasNext=false
 * <p>
 * [내부 클래스 관계]
 * - RecommendationItem      : 프론트에 전달되는 응답 DTO (score, commuteDistance 미포함)
 * - CachedRecommendationItem: Redis에 캐싱되는 내부 DTO (score 포함, commuteDistance 미포함)
 * → toResponseItem()으로 score를 제거하고 RecommendationItem으로 변환
 * <p>
 * [commuteDistance 정책]
 * - Top10 추천 응답: commuteDistance 미포함 (전체 매물 순위 정렬용이므로 불필요)
 * - 매물 상세 조회:  commuteDistance 포함 (RecommendationHouseDetailResponse.CommuteData)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationTop10Response {

    /**
     * 추천 매물 리스트 (기본값: 빈 리스트)
     */
    @Builder.Default
    private List<RecommendationItem> recommendations = List.of();

    /**
     * 다음 페이지 요청에 사용할 커서 ID.
     * 현재 페이지의 마지막 매물 houseId가 들어간다.
     * 프론트는 이 값을 다음 요청의 houseId 파라미터로 전달한다.
     */
    private Long nextHouseId;

    /**
     * 다음 페이지 존재 여부.
     * false이면 더 이상 불러올 매물이 없으므로 프론트는 무한 스크롤을 중단한다.
     */
    private Boolean hasNext;

    /**
     * 프론트에 전달되는 추천 매물 응답 항목.
     * CachedRecommendationItem.toResponseItem()으로 생성된다.
     * score(점수)는 내부 정렬용이므로 프론트 응답에는 포함하지 않는다.
     * commuteDistance는 상세 조회 API에서만 제공한다.
     * <p>
     * [필드 설명]
     * - houseId       : 매물 PK (CurrentHouse.id)
     * - images        : 매물 이미지 리스트 (썸네일 포함)
     * - dong          : 주소 (시도 시군구 동 형식)
     * - houseType     : 주거 형태 (예: "단독/다가구", "아파트")
     * - rentType      : 임대 유형 (예: "전세", "월세")
     * - houseStatus   : 매물 상태 (예: "거래가능")
     * - deposit       : 보증금 (만원)
     * - monthlyCost   : 월세 (만원)
     * - managementCost: 관리비 (만원)
     * - managementItems: 관리비 포함 항목 (예: "수도, 인터넷")
     * - floorSize     : 전용면적 (㎡)
     * - floor         : 층 정보 (예: "3층")
     * - commuteTime   : 통근 시간 (분)
     * - latitude      : 매물 위도 (지도 표시용)
     * - longitude     : 매물 경도 (지도 표시용)
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        private Long houseId;
        private List<RecommendationHouseListResponse.ImageInfo> images;
        private String dong;
        private String houseType;
        private String rentType;
        private String houseStatus;
        private Integer deposit;
        private Integer monthlyCost;
        private Integer managementCost;
        private String managementItems;
        private Double floorSize;
        private String floor;
        /**
         * 통근 시간 (분). null이면 통근 정보 없음.
         */
        private Integer commuteTime;
        /**
         * 매물 위도 (WGS84). 지도 마커 표시용.
         */
        private Double latitude;
        /**
         * 매물 경도 (WGS84). 지도 마커 표시용.
         */
        private Double longitude;
        /**
         * 찜 여부
         */
        private Boolean isLiked;

        @Setter
        @Builder.Default
        private String aiMessage = "";
    }

    /**
     * Redis에 캐싱되는 추천 매물 항목 (내부용).
     * <p>
     * RecommendationItem의 모든 필드 + score(점수)를 포함한다.
     * Redis에 JSON 직렬화하여 "recommendation:top10:{userNeedId}" 키로 저장된다.
     * <p>
     * score는 사용자 니즈 기반 종합 점수(만점 100점)이다.
     * 이 score로 내림차순 정렬된 리스트가 캐시의 단위이다.
     * <p>
     * [commuteDistance 정책]
     * Top10 정렬 기준은 commuteTime(분)만 사용하므로, commuteDistance(km)는
     * 캐시 항목에도 포함하지 않는다. 거리는 상세 조회 API에서만 제공한다.
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CachedRecommendationItem {
        private Long houseId;
        private String dong;
        private String houseType;
        private String rentType;
        private String houseStatus;
        private Integer deposit;
        private Integer monthlyCost;
        private Integer managementCost;
        private String managementItems;
        private Double floorSize;
        private String floor;
        /**
         * 통근 시간 (분). 점수 계산에 사용.
         */
        private Integer commuteTime;
        /**
         * 매물 위도 (WGS84).
         */
        private Double latitude;
        /**
         * 매물 경도 (WGS84).
         */
        private Double longitude;
        /**
         * 종합 점수 (만점 100). 이 값으로 내림차순 정렬하여 순위를 결정한다.
         */
        private Double score;
        /**
         * 매물 이미지 리스트. JSON 역직렬화 시 필드명 명시 필요.
         */
        @JsonProperty("images")
        private List<RecommendationHouseListResponse.ImageInfo> images;

        /**
         * AI가 생성한 점수 산정 이유
         */
        @Setter
        @Builder.Default
        private String aiMessage = "";

        /**
         * Redis 캐시용 DTO → 프론트 응답 DTO로 변환한다.
         * score(내부 정렬용)는 프론트에 노출하지 않으므로 제외한다.
         */
        public RecommendationItem toResponseItem() {
            return RecommendationItem.builder()
                    .houseId(houseId)
                    .images(images)
                    .dong(dong)
                    .houseType(houseType)
                    .rentType(rentType)
                    .houseStatus(houseStatus)
                    .deposit(deposit)
                    .monthlyCost(monthlyCost)
                    .managementCost(managementCost)
                    .managementItems(managementItems)
                    .floorSize(floorSize)
                    .floor(floor)
                    .commuteTime(commuteTime)
                    .latitude(latitude)
                    .longitude(longitude)
                    .aiMessage(aiMessage)
                    .build();
        }
    }
}
