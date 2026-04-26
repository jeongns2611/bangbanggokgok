package com.ssafy.backend.domain.house.service;

import com.ssafy.backend.domain.house.dto.response.HouseCompareResponse;
import com.ssafy.backend.domain.user.entity.UserNeed;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class HouseCompareScoreCalculator {

    public HouseCompareResponse.ScoreSummary calculate(
            HouseCompareResponse.ComparisonData comparisonData,
            UserNeed userNeed,
            Set<String> preferredHouseTypes
    ) {
        int trafficScore = calculateTrafficScore(comparisonData, userNeed);
        int infraScore = calculateInfraScore(comparisonData);
        int safetyScore = calculateSafetyScore(comparisonData);
        int needFitScore = calculateNeedFitScore(
                comparisonData,
                userNeed,
                preferredHouseTypes,
                trafficScore,
                infraScore,
                safetyScore
        );

        return new HouseCompareResponse.ScoreSummary(
                trafficScore,
                infraScore,
                safetyScore,
                needFitScore
        );
    }

    private int calculateTrafficScore(HouseCompareResponse.ComparisonData comparisonData, UserNeed userNeed) {
        HouseCompareResponse.CommuteData commuteData = firstCommuteData(comparisonData);
        if (commuteData == null || commuteData.commuteTime() == null || userNeed.getMaxCommuteTime() == null) {
            return 0;
        }

        double timeScore = commuteData.commuteTime() <= userNeed.getMaxCommuteTime()
                ? 100.0
                : 100.0 * userNeed.getMaxCommuteTime() / commuteData.commuteTime();

        double distancePenalty = 0.0;
        if (commuteData.commuteDistance() != null) {
            distancePenalty = Math.min(15.0, Math.max(0.0, commuteData.commuteDistance() - 10.0) * 2.0);
        }

        return clampToInt(timeScore - distancePenalty);
    }

    private int calculateInfraScore(HouseCompareResponse.ComparisonData comparisonData) {
        HouseCompareResponse.InfraCount infraCount = comparisonData.infraCount();
        HouseCompareResponse.MinDist minDist = comparisonData.minDist();

        double countScore = 0.0;
        if (infraCount != null) {
            countScore += countRatioScore(infraCount.convenienceStoreCount(), 3);
            countScore += countRatioScore(infraCount.laundryCount(), 2);
            countScore += countRatioScore(infraCount.cafeCount(), 4);
            countScore += countRatioScore(infraCount.hospitalCount(), 2);
            countScore += countRatioScore(infraCount.pharmacyCount(), 2);
            countScore += countRatioScore(infraCount.busStopCount(), 4);
        }

        double distanceScore = 0.0;
        if (minDist != null) {
            distanceScore += distanceRatioScore(minDist.convenienceDist(), 300);
            distanceScore += distanceRatioScore(minDist.laundryDist(), 500);
            distanceScore += distanceRatioScore(minDist.cafeDist(), 500);
            distanceScore += distanceRatioScore(minDist.hospitalDist(), 1000);
            distanceScore += distanceRatioScore(minDist.pharmacyDist(), 1000);
            distanceScore += distanceRatioScore(minDist.subwayDist(), 800);
        }

        return clampToInt((countScore / 6.0) * 0.5 + (distanceScore / 6.0) * 0.5);
    }

    private int calculateSafetyScore(HouseCompareResponse.ComparisonData comparisonData) {
        HouseCompareResponse.DongStats dongStats = comparisonData.dongStats();
        if (dongStats == null) {
            return 0;
        }

        double baseSafetyScore = dongStats.safetyScore() == null ? 0.0 : dongStats.safetyScore();
        double facilityScore = countRatioScore(dongStats.safetyFacilityCount(), 10);
        double cctvScore = countRatioScore(dongStats.cctvCount(), 20);
        double lightScore = countRatioScore(dongStats.streetLightCount(), 30);

        return clampToInt(baseSafetyScore * 0.7 + facilityScore * 0.1 + cctvScore * 0.1 + lightScore * 0.1);
    }

    private int calculateNeedFitScore(
            HouseCompareResponse.ComparisonData comparisonData,
            UserNeed userNeed,
            Set<String> preferredHouseTypes,
            int trafficScore,
            int infraScore,
            int safetyScore
    ) {
        double categoryComposite = trafficScore * 0.35 + infraScore * 0.35 + safetyScore * 0.30;
        double categoryPart = categoryComposite * 0.6;

        double userNeedPart = 0.0;
        userNeedPart += matchesRentType(userNeed.getRentType(), comparisonData.rentType()) ? 10.0 : 0.0;
        userNeedPart += boundedRangeScore(comparisonData.deposit(), userNeed.getMinDeposit(), userNeed.getMaxDeposit(), 10.0);
        userNeedPart += boundedRangeScore(comparisonData.monthlyCost(), userNeed.getMinMonthlyRent(), userNeed.getMaxMonthlyRent(), 10.0);
        userNeedPart += minimumThresholdScore(comparisonData.floorSize(), parseFloorSize(userNeed.getMinExclusiveSize()), 10.0);
        userNeedPart += preferredHouseTypes.isEmpty() || preferredHouseTypes.contains(comparisonData.houseType()) ? 10.0 : 0.0;

        return clampToInt(categoryPart + userNeedPart);
    }

    private HouseCompareResponse.CommuteData firstCommuteData(HouseCompareResponse.ComparisonData comparisonData) {
        if (comparisonData.commuteData() == null || comparisonData.commuteData().isEmpty()) {
            return null;
        }
        return comparisonData.commuteData().get(0);
    }

    private double countRatioScore(Integer actual, int target) {
        if (actual == null || actual <= 0) {
            return 0.0;
        }
        return 100.0 * Math.min(1.0, (double) actual / target);
    }

    private double distanceRatioScore(Integer actualDistance, int targetDistance) {
        if (actualDistance == null || actualDistance <= 0) {
            return 0.0;
        }
        if (actualDistance <= targetDistance) {
            return 100.0;
        }
        return 100.0 * Math.max(0.0, (double) targetDistance / actualDistance);
    }

    private boolean matchesRentType(String userNeedRentType, String houseRentType) {
        if (userNeedRentType == null || houseRentType == null) {
            return false;
        }
        return switch (userNeedRentType) {
            case "JEONSE" -> "전세".equals(houseRentType);
            case "MONTHLY" -> "월세".equals(houseRentType);
            default -> false;
        };
    }

    private double boundedRangeScore(Number actual, Integer min, Integer max, double maxScore) {
        if (actual == null) {
            return 0.0;
        }

        double actualValue = actual.doubleValue();
        double minValue = min == null ? Double.NEGATIVE_INFINITY : min.doubleValue();
        double maxValue = max == null ? Double.POSITIVE_INFINITY : max.doubleValue();
        if (actualValue >= minValue && actualValue <= maxValue) {
            return maxScore;
        }

        double distance = actualValue < minValue ? minValue - actualValue : actualValue - maxValue;
        double finiteMin = Double.isFinite(minValue) ? Math.abs(minValue) : 0.0;
        double finiteMax = Double.isFinite(maxValue) ? Math.abs(maxValue) : 0.0;
        double base = Math.max(Math.abs(actualValue), Math.max(finiteMin, finiteMax));
        if (base == 0.0) {
            return maxScore;
        }

        double penaltyRatio = Math.min(1.0, distance / base);
        return maxScore * (1.0 - penaltyRatio);
    }

    private double minimumThresholdScore(Double actual, Double minimum, double maxScore) {
        if (actual == null) {
            return 0.0;
        }
        if (minimum == null || minimum <= 0) {
            return maxScore;
        }
        if (actual >= minimum) {
            return maxScore;
        }
        return maxScore * Math.max(0.0, actual / minimum);
    }

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

    private int clampToInt(double score) {
        return (int) Math.round(Math.max(0.0, Math.min(100.0, score)));
    }
}
