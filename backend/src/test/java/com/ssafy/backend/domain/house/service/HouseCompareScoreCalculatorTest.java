package com.ssafy.backend.domain.house.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.backend.domain.house.dto.response.HouseCompareResponse;
import com.ssafy.backend.domain.user.entity.UserNeed;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HouseCompareScoreCalculatorTest {

    private final HouseCompareScoreCalculator calculator = new HouseCompareScoreCalculator();

    @Test
    void calculate_returnsHighScoresForWellMatchedHouse() {
        UserNeed userNeed = UserNeed.builder()
                .targetAddress("서울 강남구")
                .maxCommuteTime(30)
                .rentType("MONTHLY")
                .minDeposit(1000)
                .maxDeposit(5000)
                .minMonthlyRent(40)
                .maxMonthlyRent(80)
                .minExclusiveSize("20")
                .isDefaultNeed(true)
                .build();
        HouseCompareResponse.ComparisonData comparisonData = createComparisonData(
                "원룸", "월세", 2000, 60, 24.0, 25, 8.0,
                4, 2, 6, 2, 2, 5,
                300, 500, 400, 700, 600, 900,
                12, 25, 8, 85.0
        );

        HouseCompareResponse.ScoreSummary scoreSummary =
                calculator.calculate(comparisonData, userNeed, Set.of("원룸"));

        assertThat(scoreSummary.trafficScore()).isGreaterThanOrEqualTo(80);
        assertThat(scoreSummary.infraScore()).isGreaterThanOrEqualTo(70);
        assertThat(scoreSummary.safetyScore()).isGreaterThanOrEqualTo(70);
        assertThat(scoreSummary.needFitScore()).isGreaterThanOrEqualTo(80);
    }

    @Test
    void calculate_penalizesLongCommuteAndNeedMismatch() {
        UserNeed userNeed = UserNeed.builder()
                .targetAddress("서울 강남구")
                .maxCommuteTime(30)
                .rentType("JEONSE")
                .minDeposit(5000)
                .maxDeposit(8000)
                .minMonthlyRent(0)
                .maxMonthlyRent(20)
                .minExclusiveSize("25")
                .isDefaultNeed(true)
                .build();
        HouseCompareResponse.ComparisonData comparisonData = createComparisonData(
                "투룸", "월세", 2000, 70, 18.0, 55, 18.0,
                1, 0, 1, 0, 0, 1,
                1500, 2000, 1800, 2200, 2000, 2500,
                1, 3, 0, 20.0
        );

        HouseCompareResponse.ScoreSummary scoreSummary =
                calculator.calculate(comparisonData, userNeed, Set.of("오피스텔"));

        assertThat(scoreSummary.trafficScore()).isLessThan(60);
        assertThat(scoreSummary.infraScore()).isLessThan(50);
        assertThat(scoreSummary.safetyScore()).isLessThan(50);
        assertThat(scoreSummary.needFitScore()).isLessThan(50);
    }

    private HouseCompareResponse.ComparisonData createComparisonData(
            String houseType,
            String rentType,
            Integer deposit,
            Integer monthlyCost,
            Double floorSize,
            Integer commuteTime,
            Double commuteDistance,
            Integer convenienceStoreCount,
            Integer laundryCount,
            Integer cafeCount,
            Integer hospitalCount,
            Integer pharmacyCount,
            Integer busStopCount,
            Integer convenienceDist,
            Integer laundryDist,
            Integer cafeDist,
            Integer hospitalDist,
            Integer pharmacyDist,
            Integer subwayDist,
            Integer cctvCount,
            Integer streetLightCount,
            Integer safetyFacilityCount,
            Double safetyScore
    ) {
        return new HouseCompareResponse.ComparisonData(
                List.of(),
                1L,
                "역삼동",
                houseType,
                rentType,
                "거래가능",
                deposit,
                monthlyCost,
                5,
                "인터넷",
                floorSize,
                2020,
                "설명",
                10,
                "3층",
                List.of(new HouseCompareResponse.CommuteData(commuteTime, commuteDistance)),
                new HouseCompareResponse.InfraCount(
                        convenienceStoreCount,
                        laundryCount,
                        cafeCount,
                        hospitalCount,
                        pharmacyCount,
                        busStopCount
                ),
                new HouseCompareResponse.MinDist(
                        convenienceDist,
                        laundryDist,
                        cafeDist,
                        hospitalDist,
                        pharmacyDist,
                        subwayDist
                ),
                new HouseCompareResponse.DongStats(
                        cctvCount,
                        streetLightCount,
                        safetyFacilityCount,
                        safetyScore,
                        12000,
                        9000
                ),
                null
        );
    }
}
