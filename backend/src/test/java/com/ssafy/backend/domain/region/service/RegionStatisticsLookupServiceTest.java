package com.ssafy.backend.domain.region.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ssafy.backend.domain.house.dto.response.HouseCompareResponse;
import com.ssafy.backend.domain.region.entity.RegionStatistic;
import com.ssafy.backend.domain.region.repository.RegionStatisticRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RegionStatisticsLookupServiceTest {

    private final RegionStatisticRepository regionStatisticRepository = mock(RegionStatisticRepository.class);
    private final RegionStatisticsLookupService service = new RegionStatisticsLookupService(regionStatisticRepository);

    @Test
    void getDongStats_returnsPriceMetricsWhenRegionStatisticExists() {
        RegionStatistic regionStatistic = mock(RegionStatistic.class);
        when(regionStatistic.getPorkBellyCost()).thenReturn(15000);
        when(regionStatistic.getKimbapCost()).thenReturn(4500);
        when(regionStatisticRepository.findById("11680")).thenReturn(Optional.of(regionStatistic));

        HouseCompareResponse.DongStats result = service.getDongStats("11680");

        assertThat(result.avgMeatPrice()).isEqualTo(15000);
        assertThat(result.avgMealPrice()).isEqualTo(4500);
        assertThat(result.cctvCount()).isZero();
        assertThat(result.safetyScore()).isZero();
    }
}
