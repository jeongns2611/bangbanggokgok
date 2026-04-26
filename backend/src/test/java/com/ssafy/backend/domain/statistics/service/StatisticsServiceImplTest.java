// package com.ssafy.backend.domain.statistics.service;

// import com.ssafy.backend.domain.region.repository.RegionSigunguRepository;
// import com.ssafy.backend.domain.statistics.dto.response.RegionMonthlyStatisticResponse;
// import com.ssafy.backend.domain.statistics.entity.RegionMonthlyStatistic;
// import com.ssafy.backend.domain.statistics.repository.RegionMonthlyStatisticRepository;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.util.List;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.BDDMockito.given;

// @ExtendWith(MockitoExtension.class)
// class StatisticsServiceImplTest {

//     @Mock
//     private RegionMonthlyStatisticRepository repository;

//     @Mock
//     private RegionSigunguRepository regionSigunguRepository;

//     @InjectMocks
//     private StatisticsServiceImpl statisticsService;

//     @Test
//     @DisplayName("SERVICE: DB에서 시계열 통계를 조회하여 프론트엔드 맞춤형 DTO(avgRent 등)로 매핑한다")
//     void getMonthlyRegionStatistics_MappingTest() {
//         // given
//         String sigunguCode = "11680";
        
//         RegionMonthlyStatistic entity = RegionMonthlyStatistic.builder()
//                 .sigunguCode(sigunguCode)
//                 .sggCd("1168010100")
//                 .baseYearMonth(202407)
//                 .avgDeposit(2800)
//                 .avgMonthlyCost(72)
//                 .tradeCount(150)
//                 .build();

//         given(regionSigunguRepository.findSigunguCodeByNames("서울특별시", "강남구"))
//                 .willReturn(java.util.Optional.of(sigunguCode));
        
//         given(repository.findBySigunguCodeOrderByBaseYearMonthDesc(sigunguCode))
//                 .willReturn(List.of(entity));

//         // when
//         RegionMonthlyStatisticResponse response = statisticsService.getMonthlyRegionStatistics("서울특별시", "강남구");

//         // then
//         assertThat(response).isNotNull();
//         assertThat(response.getTrends()).hasSize(1);
        
//         // Ensure entity fields are mapped securely to API JSON DTO fields
//         assertThat(response.getTrends().get(0).getBaseYearMonth()).isEqualTo(202407);
//         assertThat(response.getTrends().get(0).getAvgDeposit()).isEqualTo(2800);
//         assertThat(response.getTrends().get(0).getAvgRent()).isEqualTo(72);            // avg_monthly_cost -> avgRent
//         assertThat(response.getTrends().get(0).getTransactionCount()).isEqualTo(150);   // trade_count -> transactionCount
//     }

//     @Test
//     @DisplayName("SERVICE: DB에 해당 지역 데이터가 없으면 예외 없이 빈 trends 리스트를 반환한다")
//     void getMonthlyRegionStatistics_EmptyData_ReturnsEmptyList() {
//         // given
//         String sigunguCode = "99999";
        
//         given(regionSigunguRepository.findSigunguCodeByNames("알수없음", "알수없음"))
//                 .willReturn(java.util.Optional.of(sigunguCode));

//         given(repository.findBySigunguCodeOrderByBaseYearMonthDesc(sigunguCode))
//                 .willReturn(List.of());

//         // when
//         RegionMonthlyStatisticResponse response = statisticsService.getMonthlyRegionStatistics("알수없음", "알수없음");

//         // then
//         assertThat(response).isNotNull();
//         assertThat(response.getTrends()).isEmpty();
//     }

//     @Test
//     @DisplayName("SERVICE: sidoName과 sigunguName 파라미터 값에 무관하게 sigunguCode만으로 조회가 정상 동작한다")
//     void getMonthlyRegionStatistics_IgnoreNames_Success() {
//         // given
//         String sigunguCode = "11680";
        
//         RegionMonthlyStatistic entity = RegionMonthlyStatistic.builder()
//                 .sigunguCode(sigunguCode)
//                 .baseYearMonth(202407)
//                 .build();

//         given(regionSigunguRepository.findSigunguCodeByNames("서울특별시", "강남구"))
//                 .willReturn(java.util.Optional.of(sigunguCode));

//         given(repository.findBySigunguCodeOrderByBaseYearMonthDesc(sigunguCode))
//                 .willReturn(List.of(entity));

//         // when
//         RegionMonthlyStatisticResponse response = statisticsService.getMonthlyRegionStatistics("서울특별시", "강남구");

//         // then
//         assertThat(response).isNotNull();
//         assertThat(response.getTrends()).hasSize(1);
//         assertThat(response.getTrends().get(0).getBaseYearMonth()).isEqualTo(202407);
//     }
// }
