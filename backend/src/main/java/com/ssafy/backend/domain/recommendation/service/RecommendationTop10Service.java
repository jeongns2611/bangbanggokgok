package com.ssafy.backend.domain.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.domain.commute.service.CommuteMetricsService;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationHouseImageRow;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationHouseListResponse;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationTop10HouseSummary;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationTop10Response;
import com.ssafy.backend.domain.recommendation.repository.RecommendationRepository;
import com.ssafy.backend.domain.user.entity.UserNeed;
import com.ssafy.backend.domain.user.entity.UserNeedHouseType;
import com.ssafy.backend.domain.user.repository.UserNeedHouseTypeRepository;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Top10 추천 매물 서비스.
 * <p>
 * Redis 캐시 관리, 전체 매물 점수 계산, 커서 기반 페이징을 담당한다.
 * RecommendationService(퍼사드)로부터 위임받아 동작한다.
 * <p>
 * [전체 흐름]
 * ① Redis에 userNeedId 단위로 캐시된 순위 리스트가 있으면 재사용
 * ② 없으면 전체 활성 매물 점수 계산 → 정렬 → Redis 저장 (TTL 1시간)
 * ③ 커서(houseId) 기반으로 다음 10건을 슬라이싱해 반환
 * <p>
 * [캐시 키] "recommendation:top10:{userNeedId}"
 * [캐시 TTL] 1시간
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional // TODO 원래 조회용이었는데 쓰기 작업이 포함되어 있어서 전체 트랜잭션으로 바뀜. 쪼갤 수 있는 부분이 있는지 확인이 필요함
public class RecommendationTop10Service {

    /**
     * 한 페이지에 내려줄 추천 매물 개수 (무한 스크롤 단위)
     */
    private static final int PAGE_SIZE = 10;
    /**
     * Redis Top10 캐시 유효 시간 (1시간). 이후 재계산된다.
     */
    private static final Duration CACHE_TTL = Duration.ofHours(1);
    private final RecommendationRepository recommendationRepository;
    private final UserNeedHouseTypeRepository userNeedHouseTypeRepository;
    private final CommuteMetricsService commuteMetricsService;
    private final HouseScoreCalculator houseScoreCalculator;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ChatClient chatClient;

    @Value("classpath:/prompt/top10_reason_system.txt")
    private Resource top10ReasonSystemPrompt;
    @Value("classpath:/prompt/top10_reason_user.txt")
    private Resource top10ReasonUserPrompt;

    /**
     * Top10 추천 매물을 페이징하여 반환한다.
     *
     * @param userNeed    사용자 매물 조건 (점수 계산 기준)
     * @param lastHouseId 마지막으로 본 매물 ID (첫 조회 시 0 또는 null)
     * @return 10건 단위 추천 매물 리스트 + 페이징 메타
     */
    public RecommendationTop10Response getTop10(UserNeed userNeed, Long lastHouseId) {
        // ── Step 1: Redis 캐시 로드 or 전체 재계산 ──
        List<RecommendationTop10Response.CachedRecommendationItem> rankedItems =
                loadOrBuildCache(userNeed, lastHouseId);

        if (rankedItems.isEmpty()) {
            return RecommendationTop10Response.builder()
                    .recommendations(List.of())
                    .nextHouseId(lastHouseId)
                    .hasNext(false)
                    .build();
        }

        // ── Step 2: 커서 기반 페이징 ──
        // lastHouseId=0 → 처음부터, 그 외 → 해당 매물 다음 위치부터
        int startIndex = findStartIndex(rankedItems, lastHouseId);
        int endIndex = Math.min(startIndex + PAGE_SIZE, rankedItems.size());

        // CachedRecommendationItem → RecommendationItem 변환 (score 제거)
        List<RecommendationTop10Response.RecommendationItem> page =
                rankedItems.subList(startIndex, endIndex).stream()
                        .map(RecommendationTop10Response.CachedRecommendationItem::toResponseItem)
                        .toList();

        // ── Step 3: 무한 스크롤 메타데이터 구성 ──
        boolean hasNext = endIndex < rankedItems.size();
        Long nextHouseId = page.isEmpty()
                ? lastHouseId
                : page.get(page.size() - 1).getHouseId();

        return RecommendationTop10Response.builder()
                .recommendations(page)
                .nextHouseId(nextHouseId)
                .hasNext(hasNext)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    //  캐시 관리
    // ─────────────────────────────────────────────────────────────

    /**
     * Redis에서 Top10 순위 캐시를 로드한다. 없으면 새로 계산 후 저장한다.
     * <p>
     * [캐시 키] "recommendation:top10:{userNeedId}"
     * [캐시 값] CachedRecommendationItem 리스트의 JSON 직렬화 문자열
     */
    private List<RecommendationTop10Response.CachedRecommendationItem> loadOrBuildCache(UserNeed userNeed, Long lastHouseId) {
        String cacheKey = buildCacheKey(userNeed.getId());
        String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);

        // ── Redis 캐시 hit ──
        if (cachedValue != null && !cachedValue.isBlank()) {
            log.info("Recommendation top10 cache hit userNeedId={}", userNeed.getId());
            try {
                return objectMapper.readValue(
                        cachedValue,
                        new TypeReference<List<RecommendationTop10Response.CachedRecommendationItem>>() {
                        }
                );
            } catch (JsonProcessingException e) {
                // 직렬화 형식이 깨진 경우 삭제 후 재계산으로 fallback
                stringRedisTemplate.delete(cacheKey);
            }
        }

        // ── Redis 캐시 miss: 전체 매물 점수 계산 + 정렬 ──
        log.info("Recommendation top10 cache miss userNeedId={}", userNeed.getId());
        List<RecommendationTop10Response.CachedRecommendationItem> computed = buildRankedItems(userNeed, lastHouseId);

        try {
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(computed),
                    CACHE_TTL
            );
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "추천 결과 캐시 저장에 실패했습니다.");
        }

        return computed;
    }

    // ─────────────────────────────────────────────────────────────
    //  점수 계산 및 순위 구성
    // ─────────────────────────────────────────────────────────────

    /**
     * 전체 활성 매물을 대상으로 점수를 계산하고 내림차순 정렬한다.
     * <p>
     * [처리 순서]
     * ① 전체 활성 매물(deletedAt IS NULL, soldAt IS NULL) 조회
     * ② 사용자 선호 주거형태 Set 구성
     * ③ 이미지 IN 쿼리로 한 번에 조회 → Map<houseId, images>
     * ④ 매물마다 점수 계산 → CachedRecommendationItem 구성
     * ⑤ score 내림차순 → 동점 시 houseId 내림차순 정렬
     */
    private List<RecommendationTop10Response.CachedRecommendationItem> buildRankedItems(UserNeed userNeed, Long lastHouseId) {
        // ① 전체 활성 매물 요약 조회 (사용자가 설정한 시도/시군구로 1차 필터링)
        List<RecommendationTop10HouseSummary> houseSummaries =
                recommendationRepository.findTop10HouseSummaries(userNeed.getSidoName(), userNeed.getSigunguName());
        if (houseSummaries.isEmpty()) {
            log.info("Recommendation top10 source empty userNeedId={} sidoName={} sigunguName={}",
                    userNeed.getId(),
                    userNeed.getSidoName(),
                    userNeed.getSigunguName());
            return List.of();
        }

        // ② 사용자 선호 주거형태 집합 (예: {"아파트", "오피스텔"})
        Set<String> preferredHouseTypes = userNeedHouseTypeRepository
                .findByIdUserNeedIdOrderByCommonCodeDetailSortOrderAsc(userNeed.getId()).stream()
                .map(UserNeedHouseType::getCommonCodeDetail)
                .map(commonCodeDetail -> commonCodeDetail.getCodeName().trim())
                .collect(Collectors.toSet());

        // ③ 이미지 IN 쿼리로 한 번에 가져와 houseId별 Map으로 그룹핑
        List<Long> houseIds = houseSummaries.stream()
                .map(RecommendationTop10HouseSummary::getHouseId)
                .toList();
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

        // ④⑤ 각 매물의 점수 계산 후 정렬
        List<RecommendationTop10Response.CachedRecommendationItem> results = houseSummaries.stream()
                .map(summary -> buildCachedItem(summary, userNeed, preferredHouseTypes, imagesByHouseId))
                .sorted(Comparator
                        .comparing(RecommendationTop10Response.CachedRecommendationItem::getScore, Comparator.reverseOrder())
                        .thenComparing(RecommendationTop10Response.CachedRecommendationItem::getHouseId, Comparator.reverseOrder()))
                .toList();
        log.info("Recommendation top10 ranking built userNeedId={} candidateCount={} rankedCount={}",
                userNeed.getId(),
                houseSummaries.size(),
                results.size());

        if (lastHouseId != 0) {
            return results;
        }

        int topCount = Math.min(results.size(), 3);
        for (int i = 0; i < topCount; i++) {
            RecommendationTop10Response.CachedRecommendationItem topItem = results.get(i);
            Long topId = topItem.getHouseId();

            RecommendationTop10HouseSummary topSummary = houseSummaries.stream()
                    .filter(summary -> summary.getHouseId().equals(topId))
                    .findFirst().orElse(null);

            if (topSummary != null) {
                Integer commuteTime = commuteMetricsService.getCommuteTimeForTop10(
                        topSummary.getPosition(), userNeed.getTargetPos()
                );

                // 종합 점수 계산 (만점 100점)
                List<Double> scoreList = houseScoreCalculator.calculateScoreDetails(
                        topSummary, userNeed, preferredHouseTypes, commuteTime
                );

                Map<String, Object> map = new java.util.HashMap<>();
                map.put("itemRentType", topSummary.getRentType());
                map.put("rentTypeScore", scoreList.get(0));
                map.put("userRentType", userNeed.getRentType());
                map.put("itemHouseType", topSummary.getHouseType());
                map.put("houseTypeScore", scoreList.get(1));
                map.put("userPreferredHouseTypes", String.join(", ", preferredHouseTypes));
                map.put("depositScore", scoreList.get(2));
                map.put("userMinDeposit", userNeed.getMinDeposit());
                map.put("userMaxDeposit", userNeed.getMaxDeposit());
                map.put("itemDeposit", topSummary.getDeposit());
                map.put("monthlyScore", scoreList.get(3));
                map.put("userMinMonthly", userNeed.getMinMonthlyRent());
                map.put("userMaxMonthly", userNeed.getMaxMonthlyRent());
                map.put("itemMonthly", topSummary.getMonthlyCost());
                map.put("commuteScore", scoreList.get(4));
                map.put("userMaxCommute", userNeed.getMaxCommuteTime());
                map.put("itemCommuteTime", commuteTime != null ? commuteTime : 0);
                map.put("sizeScore", scoreList.get(5));
                map.put("userMinSize", userNeed.getMinExclusiveSize());
                map.put("itemSize", topSummary.getFloorSize());
                map.put("totalScore", topItem.getScore());

                try {
                    String aiMessage = chatClient.prompt()
                            .system(top10ReasonSystemPrompt)
                            .user(spec -> spec.text(top10ReasonUserPrompt)
                                    .params(map))
                            .call()
                            .content();

                    // 상위 매물에 aiMessage 삽입
                    topItem.setAiMessage(aiMessage);
                } catch (Exception e) {
                    log.error("Top {} 매물 AI 메시지 생성 중 오류 발생: {}", i + 1, e.getMessage());
                    topItem.setAiMessage("AI 추천 사유를 생성할 수 없습니다.");
                }
            }
        }

        return results;
    }

    /**
     * 단일 매물에 대해 CachedRecommendationItem을 구성한다.
     * <p>
     * commuteDistance는 Top10에서 제공하지 않으므로 commuteTime만 조회한다.
     *
     * @param summary             매물 기본 정보
     * @param userNeed            사용자 매물 조건
     * @param preferredHouseTypes 사용자 선호 주거형태 Set
     * @param imagesByHouseId     미리 조회해둔 이미지 Map
     */
    private RecommendationTop10Response.CachedRecommendationItem buildCachedItem(
            RecommendationTop10HouseSummary summary,
            UserNeed userNeed,
            Set<String> preferredHouseTypes,
            Map<Long, List<RecommendationHouseListResponse.ImageInfo>> imagesByHouseId
    ) {
        // 통근 시간 조회 (Top10: commuteDistance는 불필요, commuteTime만 사용)
        Integer commuteTime = commuteMetricsService.getCommuteTimeForTop10(
                summary.getPosition(), userNeed.getTargetPos()
        );

        // 종합 점수 계산 (만점 100점)
        double score = houseScoreCalculator.calculateScore(
                summary, userNeed, preferredHouseTypes, commuteTime
        );

        // JTS Point: getX()=경도(longitude), getY()=위도(latitude)
        Double latitude = summary.getPosition() == null ? null : summary.getPosition().getY();
        Double longitude = summary.getPosition() == null ? null : summary.getPosition().getX();

        return RecommendationTop10Response.CachedRecommendationItem.builder()
                .houseId(summary.getHouseId())
                .images(imagesByHouseId.getOrDefault(summary.getHouseId(), List.of()))
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
                .commuteTime(commuteTime)
                .latitude(latitude)
                .longitude(longitude)
                .score(score)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    //  페이징 유틸리티
    // ─────────────────────────────────────────────────────────────

    /**
     * 커서 기반 페이징: lastHouseId의 위치를 찾아 그 다음 인덱스를 반환한다.
     *
     * @param rankedItems 점수 내림차순 정렬된 전체 리스트
     * @param lastHouseId 프론트가 보낸 마지막 매물 ID (null 또는 0이면 처음부터)
     * @return 다음 페이지 시작 인덱스
     */
    private int findStartIndex(
            List<RecommendationTop10Response.CachedRecommendationItem> rankedItems,
            Long lastHouseId
    ) {
        if (lastHouseId == null || lastHouseId == 0L) {
            return 0;
        }

        for (int i = 0; i < rankedItems.size(); i++) {
            if (rankedItems.get(i).getHouseId().equals(lastHouseId)) {
                return i + 1;
            }
        }

        // lastHouseId를 못 찾으면 처음부터 (캐시 갱신 등으로 리스트가 변경된 경우)
        return 0;
    }

    /**
     * Redis Top10 캐시 키를 생성한다.
     * 형식: "recommendation:top10:{userNeedId}"
     */
    private String buildCacheKey(Long userNeedId) {
        return "recommendation:top10:" + userNeedId;
    }
}
