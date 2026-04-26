package com.ssafy.backend.domain.recommendation.service;

import com.ssafy.backend.domain.recommendation.dto.response.RecommendationTop10HouseSummary;
import com.ssafy.backend.domain.user.entity.UserNeed;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 매물 점수 계산 컴포넌트.
 * <p>
 * 사용자 니즈(UserNeed)와 매물 정보를 비교하여 0~100점 사이의 종합 점수를 산출한다.
 * 점수 계산에만 집중하는 순수 로직 클래스로, 외부 의존성(DB/Redis/API)이 없다.
 * <p>
 * [점수 구성 — 합계 100점]
 * 항목               | 만점  | 우선순위
 * ─────────────────  | ───── | ──────
 * 보증금(deposit)    | 20점  | 1순위
 * 월세(monthlyCost)  | 20점  | 1순위
 * 주거형태(houseType)| 25점  | 2순위
 * 통근시간(commute)  | 15점  | 3순위
 * 전용면적(floorSize)| 10점  | 4순위
 * 임대유형(rentType) | 10점  | (보조)
 */
@Component
public class HouseScoreCalculator {

    /**
     * 매물의 종합 점수를 계산한다 (0~100점).
     *
     * @param summary             매물 기본 정보 (DB 조회 결과)
     * @param userNeed            사용자 매물 조건 (점수 계산 기준)
     * @param preferredHouseTypes 사용자 선호 주거형태 Set (예: {"아파트", "오피스텔"})
     * @param commuteTime         통근 시간(분). null이면 통근 점수 0점.
     * @return 0~100 사이의 종합 점수 (double)
     */
    public double calculateScore(
            RecommendationTop10HouseSummary summary,
            UserNeed userNeed,
            Set<String> preferredHouseTypes,
            Integer commuteTime
    ) {
        // 임대유형: UserNeed의 rentType(전세, 월세) 코드 ↔ 매물의 rentType명 비교 (10점)
        double rentTypeScore = matchesRentType(userNeed.getRentType(), summary.getRentType()) ? 10.0 : 0.0;

        // 보증금: [minDeposit, maxDeposit] 범위 내 → 20점 만점, 벗어나면 거리 비율 감점
        double depositScore = boundedRangeScore(summary.getDeposit(), userNeed.getMinDeposit(), userNeed.getMaxDeposit(), 20.0);

        // 월세: [minMonthlyRent, maxMonthlyRent] 범위 내 → 20점 만점
        double monthlyScore = boundedRangeScore(summary.getMonthlyCost(), userNeed.getMinMonthlyRent(), userNeed.getMaxMonthlyRent(), 20.0);

        // 주거형태: 선호 목록에 포함되거나 선호 목록이 비어있으면 → 25점 만점
        double houseTypeScore = preferredHouseTypes.isEmpty() || preferredHouseTypes.contains(summary.getHouseType()) ? 25.0 : 0.0;

        // 통근시간: maxCommuteTime 이내 → 15점 만점, 초과 시 비율 감점
        double commuteScore = calculateCommuteScore(commuteTime, userNeed.getMaxCommuteTime(), 15.0);

        // 전용면적: minExclusiveSize 이상 → 10점 만점, 미달 시 비율 감점
        double sizeScore = minimumThresholdScore(summary.getFloorSize(), parseFloorSize(userNeed.getMinExclusiveSize()), 10.0);

        return rentTypeScore + depositScore + monthlyScore + houseTypeScore + commuteScore + sizeScore;
    }

    /**
     * 프롬프트에 제공할 매물 평가 상세 점수 리스트를 계산합니다.
     * 순서: [임대유형, 보증금, 월세, 주거형태, 통근시간, 전용면적]
     */
    public List<Double> calculateScoreDetails(
            RecommendationTop10HouseSummary summary,
            UserNeed userNeed,
            Set<String> preferredHouseTypes,
            Integer commuteTime
    ) {
        // 임대유형 (10점)
        double rentTypeScore = matchesRentType(userNeed.getRentType(), summary.getRentType()) ? 10.0 : 0.0;

        // 보증금 (20점)
        double depositScore = boundedRangeScore(summary.getDeposit(), userNeed.getMinDeposit(), userNeed.getMaxDeposit(), 20.0);

        // 월세 (20점)
        double monthlyScore = boundedRangeScore(summary.getMonthlyCost(), userNeed.getMinMonthlyRent(), userNeed.getMaxMonthlyRent(), 20.0);

        // 주거형태 (25점)
        double houseTypeScore = preferredHouseTypes.isEmpty() || preferredHouseTypes.contains(summary.getHouseType()) ? 25.0 : 0.0;

        // 통근시간 (15점)
        double commuteScore = calculateCommuteScore(commuteTime, userNeed.getMaxCommuteTime(), 15.0);

        // 전용면적 (10점)
        double sizeScore = minimumThresholdScore(summary.getFloorSize(), parseFloorSize(userNeed.getMinExclusiveSize()), 10.0);

        return List.of(rentTypeScore, depositScore, monthlyScore, houseTypeScore, commuteScore, sizeScore);
    }

    /**
     * RecommendationRegionTopService에서 사용할 지역구용 매물 점수 계산.
     * Top10과 동일한 가중치(총 100점)를 유지하되, UserNeed 대신 요청 파라미터를 직접 받는다.
     */
    public double calculateRegionScore(
            RecommendationRegionCandidate candidate,
            RecommendationRegionPreference preference,
            Integer commuteTime,
            Integer maxCommuteTime
    ) {
        double rentTypeScore = matchesRentTypeLabel(preference.expectedRentType(), candidate.rentType()) ? 10.0 : 0.0;
        double depositScore = boundedRangeScore(
                candidate.deposit(),
                preference.minDeposit(),
                preference.maxDeposit(),
                20.0
        );
        double monthlyScore = boundedRangeScore(
                candidate.monthlyRent(),
                preference.minMonthlyRent(),
                preference.maxMonthlyRent(),
                20.0
        );
        double houseTypeScore = matchesPreferredHouseType(preference.expectedHouseTypes(), candidate.houseType()) ? 25.0 : 0.0;
        double commuteScore = calculateCommuteScore(commuteTime, maxCommuteTime, 15.0);
        double sizeScore = minimumThresholdScore(candidate.floorSize(), preference.minFloorSize(), 10.0);

        return rentTypeScore + depositScore + monthlyScore + houseTypeScore + commuteScore + sizeScore;
    }

    /**
     * 지역구 추천에서 외부 계산 통근시간(분)을 통근 점수(15점 만점)로 변환할 때 사용.
     */
    public double calculateCommuteScoreComponent(Integer commuteTime, Integer maxCommuteTime) {
        return calculateCommuteScore(commuteTime, maxCommuteTime, 15.0);
    }

    /**
     * UserNeed의 rentType 코드와 매물의 실제 임대유형명을 비교한다.
     * UserNeed: "JEONSE"/"MONTHLY" (영문 코드)
     * 매물:     "전세"/"월세" (common_code_detail.codeName)
     */
    private boolean matchesRentType(String userNeedRentType, String houseRentType) {
        if (userNeedRentType == null) {
            return false;
        }
        return switch (userNeedRentType) {
            case "JEONSE" -> "전세".equals(houseRentType);
            case "MONTHLY" -> "월세".equals(houseRentType);
            default -> false;
        };
    }

    private boolean matchesRentTypeLabel(String expectedRentType, String houseRentType) {
        if (expectedRentType == null || houseRentType == null) {
            return false;
        }

        String normalizedExpected = normalizeRentTypeLabel(expectedRentType);
        String normalizedHouse = normalizeRentTypeLabel(houseRentType);
        return normalizedExpected != null && normalizedExpected.equals(normalizedHouse);
    }

    private String normalizeRentTypeLabel(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        String upper = trimmed.toUpperCase();

        return switch (upper) {
            case "JEONSE" -> "전세";
            case "MONTHLY" -> "월세";
            default -> switch (trimmed) {
                case "전세" -> "전세";
                case "월세" -> "월세";
                default -> null;
            };
        };
    }

    private boolean matchesPreferredHouseType(Set<String> expectedHouseTypes, String actual) {
        if (expectedHouseTypes == null || expectedHouseTypes.isEmpty() || actual == null) {
            return false;
        }
        String normalizedActual = actual.trim();
        return expectedHouseTypes.stream()
                .filter(type -> type != null && !type.isBlank())
                .map(String::trim)
                .anyMatch(normalizedActual::equals);
    }

    /**
     * 값이 [min, max] 범위 내이면 만점, 벗어나면 거리 비율로 부분 점수를 부여한다.
     * <p>
     * 예) 보증금 범위 [1000, 5000], 실제 보증금 6000
     * 벗어난 거리=1000, base=6000, penaltyRatio=1000/6000=0.167
     * 점수 = 20 * (1 - 0.167) = 16.67
     *
     * @param actual   매물의 실제 값
     * @param min      사용자 지정 최소값 (null이면 하한 없음)
     * @param max      사용자 지정 최대값 (null이면 상한 없음)
     * @param maxScore 이 항목의 만점
     */
    private double boundedRangeScore(Number actual, Integer min, Integer max, double maxScore) {
        if (actual == null) {
            return 0.0;
        }

        double actualValue = actual.doubleValue();
        double minValue = (min == null) ? Double.NEGATIVE_INFINITY : min.doubleValue();
        double maxValue = (max == null) ? Double.POSITIVE_INFINITY : max.doubleValue();

        // 범위 내면 만점
        if (actualValue >= minValue && actualValue <= maxValue) {
            return maxScore;
        }

        // 범위를 벗어난 거리 계산
        double distance = (actualValue < minValue) ? (minValue - actualValue) : (actualValue - maxValue);
        double finiteMin = Double.isFinite(minValue) ? Math.abs(minValue) : 0.0;
        double finiteMax = Double.isFinite(maxValue) ? Math.abs(maxValue) : 0.0;
        double base = Math.max(Math.abs(actualValue), Math.max(finiteMin, finiteMax));

        if (base == 0) {
            return maxScore;
        }

        // 벗어난 비율만큼 감점 (최대 100% 감점)
        double penaltyRatio = Math.min(1.0, distance / base);
        return maxScore * (1.0 - penaltyRatio);
    }

    /**
     * 값이 최소 기준(minimum) 이상이면 만점, 미달이면 비율로 부분 점수를 부여한다.
     * 전용면적 점수 계산에 사용된다.
     * <p>
     * 예) 최소 면적 20㎡, 실제 면적 15㎡ → 10 * (15/20) = 7.5점
     */
    private double minimumThresholdScore(Double actual, Double minimum, double maxScore) {
        if (actual == null) {
            return 0.0;
        }
        if (minimum == null || minimum <= 0) {
            return maxScore;  // 기준이 없으면 만점
        }
        if (actual >= minimum) {
            return maxScore;
        }
        return maxScore * Math.max(0.0, actual / minimum);
    }

    /**
     * 통근시간 점수를 계산한다.
     * 최대 허용 시간 이내이면 만점, 초과 시 (허용시간/실제시간) 비율로 감점.
     * <p>
     * 예) 최대 허용 30분, 실제 45분 → 15 * (30/45) = 10점
     * 예) 통근 정보 없음(null) → 0점
     *
     * @param commuteTime    실제 통근 시간 (분)
     * @param maxCommuteTime 사용자 허용 최대 통근 시간 (분)
     * @param maxScore       이 항목의 만점
     */
    private double calculateCommuteScore(Integer commuteTime, Integer maxCommuteTime, double maxScore) {
        if (commuteTime == null || maxCommuteTime == null || maxCommuteTime <= 0) {
            return 0.0;
        }
        if (commuteTime <= maxCommuteTime) {
            return maxScore;
        }
        return maxScore * Math.max(0.0, (double) maxCommuteTime / commuteTime);
    }

    /**
     * 문자열로 저장된 전용면적을 Double로 파싱한다.
     * UserNeed.minExclusiveSize가 String 타입이기 때문에 변환이 필요하다.
     * 파싱 실패 시 null 반환 → 면적 점수는 만점으로 처리됨.
     */
    private Double parseFloorSize(String floorSize) {
        if (floorSize == null || floorSize.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(floorSize);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public record RecommendationRegionCandidate(
            Integer deposit,
            Integer monthlyRent,
            String houseType,
            String rentType,
            Double floorSize
    ) {
    }

    public record RecommendationRegionPreference(
            Integer minDeposit,
            Integer maxDeposit,
            Integer minMonthlyRent,
            Integer maxMonthlyRent,
            Double minFloorSize,
            Set<String> expectedHouseTypes,
            String expectedRentType
    ) {
    }
}
