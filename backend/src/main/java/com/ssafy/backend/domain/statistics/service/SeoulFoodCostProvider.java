package com.ssafy.backend.domain.statistics.service;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import java.util.SplittableRandom;
import org.springframework.stereotype.Component;

@Component
public class SeoulFoodCostProvider {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final SigunguFoodCostRange DEFAULT_RANGE = new SigunguFoodCostRange(
            new FoodCostRange(5_800, 7_400),
            new FoodCostRange(15_500, 20_500)
    );

    private static final Map<String, SigunguFoodCostRange> RANGE_BY_SIGUNGU_CODE = Map.ofEntries(
            Map.entry("11110", new SigunguFoodCostRange(new FoodCostRange(5_900, 7_200), new FoodCostRange(16_500, 19_500))),
            Map.entry("11140", new SigunguFoodCostRange(new FoodCostRange(5_800, 7_100), new FoodCostRange(16_000, 19_000))),
            Map.entry("11170", new SigunguFoodCostRange(new FoodCostRange(6_200, 7_700), new FoodCostRange(17_500, 21_000))),
            Map.entry("11200", new SigunguFoodCostRange(new FoodCostRange(5_700, 7_000), new FoodCostRange(15_500, 18_800))),
            Map.entry("11215", new SigunguFoodCostRange(new FoodCostRange(5_800, 7_200), new FoodCostRange(16_000, 19_200))),
            Map.entry("11230", new SigunguFoodCostRange(new FoodCostRange(5_600, 6_900), new FoodCostRange(15_200, 18_200))),
            Map.entry("11260", new SigunguFoodCostRange(new FoodCostRange(5_500, 6_800), new FoodCostRange(15_000, 18_000))),
            Map.entry("11290", new SigunguFoodCostRange(new FoodCostRange(5_600, 6_900), new FoodCostRange(15_200, 18_300))),
            Map.entry("11305", new SigunguFoodCostRange(new FoodCostRange(5_400, 6_700), new FoodCostRange(14_800, 17_800))),
            Map.entry("11320", new SigunguFoodCostRange(new FoodCostRange(5_400, 6_700), new FoodCostRange(14_700, 17_700))),
            Map.entry("11350", new SigunguFoodCostRange(new FoodCostRange(5_600, 6_900), new FoodCostRange(15_000, 18_200))),
            Map.entry("11380", new SigunguFoodCostRange(new FoodCostRange(5_600, 6_900), new FoodCostRange(15_100, 18_200))),
            Map.entry("11410", new SigunguFoodCostRange(new FoodCostRange(5_700, 7_000), new FoodCostRange(15_500, 18_700))),
            Map.entry("11440", new SigunguFoodCostRange(new FoodCostRange(5_900, 7_200), new FoodCostRange(16_300, 19_500))),
            Map.entry("11470", new SigunguFoodCostRange(new FoodCostRange(5_700, 7_000), new FoodCostRange(15_600, 18_800))),
            Map.entry("11500", new SigunguFoodCostRange(new FoodCostRange(5_700, 7_100), new FoodCostRange(15_800, 19_000))),
            Map.entry("11530", new SigunguFoodCostRange(new FoodCostRange(5_500, 6_800), new FoodCostRange(15_000, 18_000))),
            Map.entry("11545", new SigunguFoodCostRange(new FoodCostRange(5_400, 6_700), new FoodCostRange(14_700, 17_800))),
            Map.entry("11560", new SigunguFoodCostRange(new FoodCostRange(5_900, 7_300), new FoodCostRange(16_300, 19_700))),
            Map.entry("11590", new SigunguFoodCostRange(new FoodCostRange(5_900, 7_200), new FoodCostRange(16_200, 19_500))),
            Map.entry("11620", new SigunguFoodCostRange(new FoodCostRange(5_500, 6_900), new FoodCostRange(15_200, 18_300))),
            Map.entry("11650", new SigunguFoodCostRange(new FoodCostRange(6_500, 8_000), new FoodCostRange(18_500, 22_500))),
            Map.entry("11680", new SigunguFoodCostRange(new FoodCostRange(6_800, 8_400), new FoodCostRange(19_500, 24_000))),
            Map.entry("11710", new SigunguFoodCostRange(new FoodCostRange(6_200, 7_700), new FoodCostRange(17_500, 21_200))),
            Map.entry("11740", new SigunguFoodCostRange(new FoodCostRange(5_900, 7_300), new FoodCostRange(16_300, 19_700)))
    );

    public FoodCost getFoodCost(String sigunguCode) {
        String normalizedCode = normalizeCode(sigunguCode);
        SigunguFoodCostRange range = RANGE_BY_SIGUNGU_CODE.getOrDefault(normalizedCode, DEFAULT_RANGE);
        String monthKey = YearMonth.now(KOREA_ZONE).toString();

        int avgMealPrice = pickValue(normalizedCode, "meal", monthKey, range.mealPriceRange());
        int avgMeatPrice = pickValue(normalizedCode, "meat", monthKey, range.meatPriceRange());

        return new FoodCost(avgMeatPrice, avgMealPrice);
    }

    private int pickValue(String sigunguCode, String metric, String monthKey, FoodCostRange range) {
        if (range.max() <= range.min()) {
            return range.min();
        }

        long seed = Objects.hash(sigunguCode, metric, monthKey, range.min(), range.max());
        SplittableRandom random = new SplittableRandom(seed);
        return range.min() + random.nextInt(range.max() - range.min() + 1);
    }

    private String normalizeCode(String sigunguCode) {
        return sigunguCode == null ? "" : sigunguCode.trim();
    }

    public record FoodCost(Integer avgMeatPrice, Integer avgMealPrice) {
    }

    private record SigunguFoodCostRange(FoodCostRange mealPriceRange, FoodCostRange meatPriceRange) {
    }

    private record FoodCostRange(int min, int max) {
    }
}
