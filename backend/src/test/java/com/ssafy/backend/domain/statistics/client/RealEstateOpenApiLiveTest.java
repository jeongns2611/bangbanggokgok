// package com.ssafy.backend.domain.statistics.client;

// import com.ssafy.backend.domain.statistics.dto.openapi.OfficetelRentItemDto;
// import com.ssafy.backend.domain.statistics.dto.openapi.OpenApiResponseDto;
// import com.ssafy.backend.domain.statistics.dto.openapi.SingleFamilyRentItemDto;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.ActiveProfiles;

// import static org.assertj.core.api.Assertions.assertThat;

// @SpringBootTest
// @ActiveProfiles("test")
// @EnabledIfSystemProperty(named = "runLiveApiTests", matches = "true")
// public class RealEstateOpenApiLiveTest {

//     @Autowired
//     private RealEstateOpenApiClient apiClient;

//     @Test
//     @DisplayName("실제 공공데이터 포털 단독다가구 전월세 API 연동 테스트")
//     void testLiveSingleFamilyRentApi() {
//         // given
//         String lawdCd = "11110"; // 종로구
//         String dealYmd = "202407"; // 2024년 7월

//         System.out.println("\n[1] 단독다가구 전월세 실거래가 API 호출 시작...");

//         // when
//         OpenApiResponseDto<SingleFamilyRentItemDto> response = apiClient.fetchSingleFamilyRent(lawdCd, dealYmd, 1).block();

//         // then
//         System.out.println("\n====== 단독다가구 실제 파싱 결과 ======");
//         System.out.println("Response Header: " + response.getHeader());
        
//         if (response.getBody() != null) {
//             System.out.println("Total Count: " + response.getBody().getTotalCount());
//             System.out.println("Num of rows: " + response.getBody().getNumOfRows());
//             if (response.getBody().getItems() != null && !response.getBody().getItems().isEmpty()) {
//                 System.out.println("[Data Sample - First Item]");
//                 System.out.println(response.getBody().getItems().get(0));
//             } else {
//                 System.out.println("[Data] No items found.");
//             }
//         }
//         System.out.println("=====================================\n");

//         assertThat(response).isNotNull();
//         assertThat(response.getHeader().getResultCode()).isEqualTo("000"); // 정상 코드
//     }

//     @Test
//     @DisplayName("실제 공공데이터 포털 오피스텔 전월세 API 연동 테스트")
//     void testLiveOfficetelRentApi() {
//         // given
//         String lawdCd = "11110";
//         String dealYmd = "202407";
        
//         System.out.println("\n[2] 오피스텔 전월세 실거래가 API 호출 시작...");

//         // when
//         OpenApiResponseDto<OfficetelRentItemDto> response = apiClient.fetchOfficetelRent(lawdCd, dealYmd, 1).block();

//         // then
//         System.out.println("\n====== 오피스텔 실제 파싱 결과 ======");
//         System.out.println("Response Header: " + response.getHeader());
        
//         if (response.getBody() != null) {
//             System.out.println("Total Count: " + response.getBody().getTotalCount());
//             System.out.println("Num of rows: " + response.getBody().getNumOfRows());
//             if (response.getBody().getItems() != null && !response.getBody().getItems().isEmpty()) {
//                 System.out.println("[Data Sample - First Item]");
//                 System.out.println(response.getBody().getItems().get(0));
//             } else {
//                  System.out.println("[Data] No items found.");
//             }
//         }
//         System.out.println("=====================================\n");

//         assertThat(response).isNotNull();
//         assertThat(response.getHeader().getResultCode()).isEqualTo("000"); // 정상 코드
//     }
// }
