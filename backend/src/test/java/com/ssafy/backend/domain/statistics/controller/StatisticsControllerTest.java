// package com.ssafy.backend.domain.statistics.controller;

// import static org.mockito.BDDMockito.given;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// import com.ssafy.backend.domain.statistics.dto.response.RegionMonthlyStatisticResponse;
// import com.ssafy.backend.domain.statistics.dto.response.StatisticTrendDto;
// import com.ssafy.backend.domain.statistics.service.StatisticsService;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.web.servlet.MockMvc;

// import java.util.List;

// import static org.mockito.BDDMockito.given;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

// @SpringBootTest
// @AutoConfigureMockMvc
// @WithMockUser
// class StatisticsControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private StatisticsService statisticsService;

//     @MockBean
//     private com.ssafy.backend.global.auth.jwt.JwtAuthenticationFilter jwtAuthenticationFilter;

//     @org.junit.jupiter.api.BeforeEach
//     void setUp() throws Exception {
//         org.mockito.Mockito.doAnswer(invocation -> {
//             jakarta.servlet.http.HttpServletRequest request = invocation.getArgument(0);
//             jakarta.servlet.http.HttpServletResponse response = invocation.getArgument(1);
//             jakarta.servlet.FilterChain chain = invocation.getArgument(2);
//             chain.doFilter(request, response);
//             return null;
//         }).when(jwtAuthenticationFilter).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
//     }

//     @Test
//     @DisplayName("API: 특정 지역의 월별 거래/시세 통계를 성공적으로 응답한다")
//     void getMonthlyRegionStatistics() throws Exception {
//         // given
//         String sidoName = "서울특별시";
//         String sigunguName = "강남구";

//         List<StatisticTrendDto> trends = List.of(
//                 StatisticTrendDto.builder()
//                         .baseYearMonth(202407)
//                         .avgDeposit(2800)
//                         .avgRent(72)
//                         .transactionCount(150)
//                         .build()
//         );

//         RegionMonthlyStatisticResponse mockResponse = RegionMonthlyStatisticResponse.builder()
//                 .trends(trends)
//                 .build();

//         given(statisticsService.getMonthlyRegionStatistics(sidoName, sigunguName))
//                 .willReturn(mockResponse);

//         // when & then
//         mockMvc.perform(get("/api/v1/statistics/regions/monthly")
//                         .param("sidoName", sidoName)
//                         .param("sigunguName", sigunguName))
//                 .andDo(print())
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.code").value(200)) // ApiResponse Success code
//                 .andExpect(jsonPath("$.data.trends").isArray())
//                 .andExpect(jsonPath("$.data.trends[0].baseYearMonth").value(202407))
//                 .andExpect(jsonPath("$.data.trends[0].avgDeposit").value(2800))
//                 .andExpect(jsonPath("$.data.trends[0].avgRent").value(72))
//                 .andExpect(jsonPath("$.data.trends[0].transactionCount").value(150));
//     }

//     @Test
//     @DisplayName("API: 필수 파라미터(sidoName)가 누락되면 400 Bad Request를 반환해야 하나, 현재 설정상 500이 날 수 있음 (확인 필요)")
//     void getMonthlyRegionStatistics_MissingSidoName_ThrowsError() throws Exception {
//         // given
//         String sigunguName = "강남구";

//         // when & then
//         mockMvc.perform(get("/api/v1/statistics/regions/monthly")
//                         .param("sigunguName", sigunguName))
//                 .andDo(print())
//                 .andExpect(status().isInternalServerError())
//                 .andExpect(jsonPath("$.code").value(500));
//     }

//     @Test
//     @DisplayName("API: 필수 파라미터(sigunguName)가 누락되면 500 Internal Server Error를 반환한다")
//     void getMonthlyRegionStatistics_MissingSigungName_ThrowsError() throws Exception {
//         // given
//         String sidoName = "서울특별시";

//         // when & then
//         mockMvc.perform(get("/api/v1/statistics/regions/monthly")
//                         .param("sidoName", sidoName))
//                 .andDo(print())
//                 .andExpect(status().isInternalServerError())
//                 .andExpect(jsonPath("$.code").value(500));
//     }

//     // 기존의 MissingSigunguCode 테스트는 더 이상 유효하지 않으므로 삭제하거나 수정이 필요합니다.
//     // sigunguCode가 더 이상 경로 변수가 아니기 때문입니다.
// }
