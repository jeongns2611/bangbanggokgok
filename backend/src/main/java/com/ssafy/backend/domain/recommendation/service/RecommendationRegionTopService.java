package com.ssafy.backend.domain.recommendation.service;

import com.ssafy.backend.domain.code.entity.CommonCodeDetail;
import com.ssafy.backend.domain.code.repository.CommonCodeDetailRepository;
import com.ssafy.backend.domain.recommendation.dto.request.RecommendationRegionTopRequest;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationRegionTopResponse;
import com.ssafy.backend.domain.recommendation.repository.RecommendationRegionTopHouseRow;
import com.ssafy.backend.domain.recommendation.repository.RecommendationRegionTopRepository;
import com.ssafy.backend.domain.recommendation.repository.RecommendationRegionTopRow;
import com.ssafy.backend.domain.region.service.RegionService;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 지역구 TOP3 추천/통계 API 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationRegionTopService {

    private static final Long HOUSE_TYPE_GROUP_ID = 1L;
    private static final Long RENT_TYPE_GROUP_ID = 2L;
    private static final int TOP_N = 3;
    private static final int SEOUL_AVG_FOOD_COST = 20_000;
    private static final int DEFAULT_MAX_COMMUTE_TIME = 100;
    private static final double MATCH_SCORE_THRESHOLD = 60.0;

    private final CommonCodeDetailRepository commonCodeDetailRepository;
    private final RecommendationRegionTopRepository recommendationRegionTopRepository;
    private final RegionService regionService;
    private final RecommendationCalcComuteTimeService recommendationCalcComuteTimeService;
    private final HouseScoreCalculator houseScoreCalculator;

    public RecommendationRegionTopResponse getTopRegionRecommendations(RecommendationRegionTopRequest request) {
        List<RecommendationRegionTopResponse.CodeInfo> houseTypes = resolveActiveHouseTypes(request.getHouseType());
        RecommendationRegionTopResponse.CodeInfo primaryHouseType = houseTypes.get(0);
        RecommendationRegionTopResponse.CodeInfo rentType = resolveActiveRentType(request.getRentType());
        String sidoCode = regionService.getSidoCode(request.getSidoName());

        IntRange depositRange = buildIntegerRange(request.getMinDeposit(), request.getMaxDeposit(), "deposit");
        IntRange monthlyRange = buildMonthlyRentRange(
                rentType.getCodeName(),
                request.getMinMonthlyRent(),
                request.getMaxMonthlyRent()
        );
        DoubleRange floorSizeRange = buildFloorSizeRange(request.getMinFloorSize());

        // 1) 지역구별 대표 통근시간(랜덤 1매물 기준) 선 계산
        Map<String, Integer> commuteMinutesByRegion = buildCommuteMinutesByRegion(
                request.getDestinationLatitude(),
                request.getDestinationLongitude()
        );

        // 2) 시도 내 활성 매물 전체 조회 + 지역 메타(시설/물가) 조회
        List<RecommendationRegionTopHouseRow> activeHouses = recommendationRegionTopRepository.findActiveHousesBySido(sidoCode);
        List<RecommendationRegionTopRow> regionStatics = recommendationRegionTopRepository.findRegionStatics(sidoCode);

        // 3) 매물별 점수 계산 후 임계치(>=60) 통과 매물만 지역구별 집계
        Set<String> preferredHouseTypes = houseTypes.stream()
                .map(RecommendationRegionTopResponse.CodeInfo::getCodeName)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        HouseScoreCalculator.RecommendationRegionPreference preference =
                new HouseScoreCalculator.RecommendationRegionPreference(
                        depositRange.min(),
                        depositRange.max(),
                        monthlyRange.min(),
                        monthlyRange.max(),
                        floorSizeRange.min(),
                        preferredHouseTypes,
                        rentType.getCodeName()
                );

        Map<String, RegionScoreAggregate> aggregateByRegion = aggregateScoredHouses(
                activeHouses,
                preference,
                commuteMinutesByRegion
        );

        // 4) 기존 시설/물가 정보와 점수 집계 결과를 병합하여 지역구 추천 아이템 생성
        List<ScoredRecommendationItem> scoredItems = regionStatics.stream()
                .map(row -> toScoredRecommendationItem(
                        row,
                        rentType,
                        commuteMinutesByRegion.get(row.getRegionCode()),
                        aggregateByRegion.get(row.getRegionCode())
                ))
                .sorted(buildScoreComparator())
                .toList();

        List<RecommendationRegionTopResponse.RecommendationItem> ranked = IntStream.range(0, scoredItems.size())
                .mapToObj(index -> applyRank(scoredItems.get(index).item(), index + 1))
                .toList();

        return RecommendationRegionTopResponse.builder()
                .appliedFilter(RecommendationRegionTopResponse.AppliedFilter.builder()
                        .sidoName(request.getSidoName())
                        .houseType(primaryHouseType)
                        .rentType(rentType)
                        .priceRangePolicy("SOFT_SCORING")
                        .areaRangePolicy("SOFT_SCORING_MIN")
                        .houseTypePolicy("SOFT_SCORING")
                        .build())
                .recommendations(ranked)
                .meta(RecommendationRegionTopResponse.Meta.builder()
                        .topN(TOP_N)
                        .totalDistrictCount(ranked.size())
                        .baseCodePolicy("common_code_detail")
                        .excludedInactiveCodes(true)
                        .sortPolicy("SCORE_SUM_DESC_THEN_MATCHING_HOUSE_DESC")
                        .build())
                .build();
    }

    private Map<String, RegionScoreAggregate> aggregateScoredHouses(
            List<RecommendationRegionTopHouseRow> activeHouses,
            HouseScoreCalculator.RecommendationRegionPreference preference,
            Map<String, Integer> commuteMinutesByRegion
    ) {
        Map<String, RegionScoreAggregate> aggregateByRegion = new HashMap<>();

        for (RecommendationRegionTopHouseRow house : activeHouses) {
            String regionCode = house.getRegionCode();
            if (regionCode == null) {
                continue;
            }

            Integer commuteMinutes = normalizeCommuteMinutes(commuteMinutesByRegion.get(regionCode));
            HouseScoreCalculator.RecommendationRegionCandidate candidate =
                    new HouseScoreCalculator.RecommendationRegionCandidate(
                            house.getDeposit(),
                            house.getMonthlyRent(),
                            house.getHouseType(),
                            house.getRentType(),
                            house.getFloorSize()
                    );

            double score = houseScoreCalculator.calculateRegionScore(
                    candidate,
                    preference,
                    commuteMinutes,
                    DEFAULT_MAX_COMMUTE_TIME
            );

            if (score < MATCH_SCORE_THRESHOLD) {
                continue;
            }

            aggregateByRegion
                    .computeIfAbsent(regionCode, key -> new RegionScoreAggregate())
                    .add(house.getDeposit(), house.getMonthlyRent(), score);
        }

        return aggregateByRegion;
    }

    private Map<String, Integer> buildCommuteMinutesByRegion(
            Double destinationLatitude,
            Double destinationLongitude
    ) {
        Map<String, Integer> result = new HashMap<>();

        recommendationCalcComuteTimeService
                .calculateCommuteTimesByDestination(destinationLatitude, destinationLongitude)
                .forEach(row -> {
                    String regionCode = asString(row.get("regionCode"));
                    Integer commuteMinutes = normalizeCommuteMinutes(asInteger(row.get("commuteTime")));
                    if (regionCode != null) {
                        result.put(regionCode, commuteMinutes);
                    }
                });

        return result;
    }

    private Comparator<ScoredRecommendationItem> buildScoreComparator() {
        return Comparator
                .comparingDouble(ScoredRecommendationItem::regionScore).reversed()
                .thenComparing(
                        scored -> scored.item().getMatchingHouseCount(),
                        Comparator.reverseOrder()
                )
                .thenComparing(
                        scored -> scored.item().getFacilityCount() == null
                                ? 0
                                : defaultZero(scored.item().getFacilityCount().getTotalCount()),
                        Comparator.reverseOrder()
                )
                .thenComparing(
                        scored -> scored.item().getRegionCode(),
                        Comparator.nullsLast(String::compareTo)
                );
    }

    private RecommendationRegionTopResponse.RecommendationItem applyRank(
            RecommendationRegionTopResponse.RecommendationItem item,
            int rank
    ) {
        boolean isTop3 = rank <= TOP_N;

        return RecommendationRegionTopResponse.RecommendationItem.builder()
                .rank(rank)
                .top3(isTop3)
                .top3Rank(isTop3 ? rank : null)
                .sigunguName(item.getSigunguName())
                .regionCode(item.getRegionCode())
                .rentType(item.getRentType())
                .hasMatchingHouses(item.getHasMatchingHouses())
                .matchingHouseCount(item.getMatchingHouseCount())
                .housingCost(item.getHousingCost())
                .facilityCount(item.getFacilityCount())
                .commute(item.getCommute())
                .livingCost(item.getLivingCost())
                .build();
    }

    private ScoredRecommendationItem toScoredRecommendationItem(
            RecommendationRegionTopRow row,
            RecommendationRegionTopResponse.CodeInfo rentType,
            Integer commuteMinutes,
            RegionScoreAggregate aggregate
    ) {
        int matchingHouseCount = aggregate == null ? 0 : aggregate.matchingHouseCount();
        boolean hasMatchingHouses = matchingHouseCount > 0;
        int avgDeposit = hasMatchingHouses ? aggregate.avgDeposit() : 0;
        int avgMonthlyRent = hasMatchingHouses ? aggregate.avgMonthlyRent() : 0;
        double regionScore = hasMatchingHouses ? aggregate.totalScore() : 0.0;

        if (isJeonse(rentType)) {
            avgMonthlyRent = 0;
        }

        int convenienceStoreCount = defaultZero(row.getConvenienceStoreCount());
        int cafeCount = defaultZero(row.getCafeCount());
        int hospitalCount = defaultZero(row.getHospitalCount());
        int totalCount = convenienceStoreCount + cafeCount + hospitalCount;

        int selectedRegionFoodCost = defaultZero(row.getSelectedRegionFoodCost());
        int foodCostIndex = calculateFoodCostIndex(selectedRegionFoodCost, SEOUL_AVG_FOOD_COST);

        RecommendationRegionTopResponse.RecommendationItem item = RecommendationRegionTopResponse.RecommendationItem.builder()
                .rank(null)
                .top3(false)
                .top3Rank(null)
                .sigunguName(row.getSigunguName())
                .regionCode(row.getRegionCode())
                .rentType(rentType)
                .hasMatchingHouses(hasMatchingHouses)
                .matchingHouseCount(matchingHouseCount)
                .housingCost(RecommendationRegionTopResponse.HousingCost.builder()
                        .avgDeposit(avgDeposit)
                        .avgMonthlyRent(avgMonthlyRent)
                        .build())
                .facilityCount(RecommendationRegionTopResponse.FacilityCount.builder()
                        .convenienceStoreCount(convenienceStoreCount)
                        .cafeCount(cafeCount)
                        .hospitalCount(hospitalCount)
                        .totalCount(totalCount)
                        .build())
                .commute(RecommendationRegionTopResponse.Commute.builder()
                        .avgCommuteMinutes(normalizeCommuteMinutes(commuteMinutes))
                        .avgCommuteDistanceKm(null)
                        .build())
                .livingCost(RecommendationRegionTopResponse.LivingCost.builder()
                        .foodCostIndex(foodCostIndex)
                        .selectedRegionFoodCost(selectedRegionFoodCost)
                        .seoulAvgFoodCost(SEOUL_AVG_FOOD_COST)
                        .build())
                .build();

        return new ScoredRecommendationItem(item, regionScore);
    }

    private List<RecommendationRegionTopResponse.CodeInfo> resolveActiveHouseTypes(List<String> houseTypeInputs) {
        if (houseTypeInputs == null || houseTypeInputs.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "주거유형 정보가 없습니다.");
        }

        return houseTypeInputs.stream()
                .map(this::normalizeHouseType)
                .distinct()
                .map(normalizedHouseType -> commonCodeDetailRepository
                        .findByCodeNameAndGroup_IdAndIsActiveTrue(normalizedHouseType, HOUSE_TYPE_GROUP_ID)
                        .map(code -> new RecommendationRegionTopResponse.CodeInfo(code.getId(), code.getCodeName()))
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.INVALID_INPUT,
                                "유효하지 않은 주거유형입니다. houseType=" + normalizedHouseType
                        )))
                .toList();
    }

    private RecommendationRegionTopResponse.CodeInfo resolveActiveRentType(String rentTypeInput) {
        String normalizedRentType = normalizeRentType(rentTypeInput);

        CommonCodeDetail rentTypeCode = commonCodeDetailRepository
                .findByCodeNameAndGroup_IdAndIsActiveTrue(normalizedRentType, RENT_TYPE_GROUP_ID)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_INPUT,
                        "유효하지 않은 거래유형입니다. rentType=" + rentTypeInput
                ));

        return new RecommendationRegionTopResponse.CodeInfo(rentTypeCode.getId(), rentTypeCode.getCodeName());
    }

    private String normalizeHouseType(String houseTypeInput) {
        if (houseTypeInput == null || houseTypeInput.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "주거유형 정보가 없습니다.");
        }
        System.out.println("[타입] : " + houseTypeInput);
        return switch (houseTypeInput.trim()) {
            case "단독/다가구", "단독/다가구(원룸)" -> "단독/다가구(원룸)";
            case "연립/다세대", "연립/다세대(빌라)" -> "연립/다세대(빌라)";
            case "오피스텔" -> "오피스텔";
            case "아파트" -> "아파트";
            default -> throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "지원하지 않는 주거유형입니다. houseType=" + houseTypeInput
            );
        };
    }

    private String normalizeRentType(String rentTypeInput) {
        if (rentTypeInput == null || rentTypeInput.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "거래유형 정보가 없습니다.");
        }

        String trimmed = rentTypeInput.trim();
        String upper = trimmed.toUpperCase();

        return switch (upper) {
            case "JEONSE" -> "전세";
            case "MONTHLY" -> "월세";
            default -> switch (trimmed) {
                case "전세" -> "전세";
                case "월세" -> "월세";
                default -> throw new BusinessException(
                        ErrorCode.INVALID_INPUT,
                        "지원하지 않는 거래유형입니다. rentType=" + rentTypeInput
                );
            };
        };
    }

    private IntRange buildIntegerRange(Integer min, Integer max, String fieldName) {
        if (min == null || max == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, fieldName + " 범위가 누락되었습니다.");
        }

        if (min > max) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    fieldName + " 최소값이 최대값보다 클 수 없습니다. min=" + min + ", max=" + max
            );
        }

        return new IntRange(min, max);
    }

    private IntRange buildMonthlyRentRange(String rentTypeCodeName, Integer minMonthlyRent, Integer maxMonthlyRent) {
        IntRange monthlyRange = buildIntegerRange(minMonthlyRent, maxMonthlyRent, "monthlyRent");

        if ("전세".equals(rentTypeCodeName) && (monthlyRange.min() != 0 || monthlyRange.max() != 0)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "전세의 경우 월세 범위는 0~0이어야 합니다.");
        }

        return monthlyRange;
    }

    private DoubleRange buildFloorSizeRange(Double minFloorSize) {
        if (minFloorSize == null || minFloorSize <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "최소 면적값이 올바르지 않습니다.");
        }

        return new DoubleRange(minFloorSize, Double.MAX_VALUE);
    }

    private int calculateFoodCostIndex(int selectedRegionFoodCost, int seoulAvgFoodCost) {
        if (selectedRegionFoodCost <= 0 || seoulAvgFoodCost <= 0) {
            return 0;
        }
        return (int) Math.round((selectedRegionFoodCost * 100.0) / seoulAvgFoodCost);
    }

    private boolean isJeonse(RecommendationRegionTopResponse.CodeInfo rentType) {
        return rentType != null && "전세".equals(rentType.getCodeName());
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Integer normalizeCommuteMinutes(Integer commuteMinutes) {
        if (commuteMinutes == null || commuteMinutes < 0) {
            return null;
        }
        return commuteMinutes;
    }

    private Integer asInteger(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private record ScoredRecommendationItem(
            RecommendationRegionTopResponse.RecommendationItem item,
            double regionScore
    ) {
    }

    private record IntRange(int min, int max) {
    }

    private record DoubleRange(double min, double max) {
    }

    private static final class RegionScoreAggregate {
        private int count;
        private long depositSum;
        private long monthlyRentSum;
        private double scoreSum;

        private void add(Integer deposit, Integer monthlyRent, double score) {
            this.count++;
            this.depositSum += deposit == null ? 0 : deposit;
            this.monthlyRentSum += monthlyRent == null ? 0 : monthlyRent;
            this.scoreSum += score;
        }

        private int matchingHouseCount() {
            return count;
        }

        private int avgDeposit() {
            return count == 0 ? 0 : (int) Math.round(depositSum / (double) count);
        }

        private int avgMonthlyRent() {
            return count == 0 ? 0 : (int) Math.round(monthlyRentSum / (double) count);
        }

        private double totalScore() {
            return scoreSum;
        }
    }
}
