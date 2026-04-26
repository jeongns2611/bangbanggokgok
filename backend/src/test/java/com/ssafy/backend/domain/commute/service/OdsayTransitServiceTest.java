// package com.ssafy.backend.domain.commute.service;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.assertThatThrownBy;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.ssafy.backend.domain.commute.dto.request.CommuteRequest;
// import com.ssafy.backend.domain.commute.dto.response.OdsayResponse;
// import com.ssafy.backend.global.error.code.ErrorCode;
// import com.ssafy.backend.global.error.exception.BusinessException;
// import java.net.URI;
// import java.util.concurrent.atomic.AtomicReference;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.test.util.ReflectionTestUtils;
// import org.springframework.web.reactive.function.client.ClientRequest;
// import org.springframework.web.reactive.function.client.ClientResponse;
// import org.springframework.web.reactive.function.client.ExchangeFunction;
// import org.springframework.web.reactive.function.client.WebClient;
// import reactor.core.publisher.Mono;

// class OdsayTransitServiceTest {

//     @Test
//     @DisplayName("ODsay 서비스는 요청 파라미터를 포함해 호출하고 응답 JSON을 DTO로 매핑한다")
//     void getCommuteInfo_success() {
//         AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
//         WebClient webClient = buildWebClient(request -> {
//             capturedRequest.set(request);
//             return jsonResponse("""
//                     {
//                       "result": {
//                         "searchType": 0,
//                         "outTrafficCheck": 0,
//                         "busCount": 10,
//                         "subwayCount": 1,
//                         "subwayBusCount": 3,
//                         "pointDistance": 0,
//                         "startRadius": 0,
//                         "endRadius": 0,
//                         "path": [
//                           {
//                             "pathType": 1,
//                             "info": {
//                               "totalTime": 37,
//                               "totalDistance": 12500.0,
//                               "payment": 1450,
//                               "busTransitCount": 0,
//                               "subwayTransitCount": 1,
//                               "firstStartStation": "강남역",
//                               "lastEndStation": "잠실역"
//                             }
//                           }
//                         ]
//                       }
//                     }
//                     """);
//         });
//         OdsayTransitService service = new OdsayTransitService(webClient, new ObjectMapper());
//         ReflectionTestUtils.setField(service, "odsayApiKey", "test-odsay-key");

//         OdsayResponse response = service.getCommuteInfo(buildRequest());

//         URI requestUri = capturedRequest.get().url();
//         assertThat(requestUri.getHost()).isEqualTo("api.odsay.com");
//         assertThat(requestUri.getPath()).isEqualTo("/v1/api/searchPubTransPathT");
//         assertThat(requestUri.getQuery()).contains("SX=127.033");
//         assertThat(requestUri.getQuery()).contains("SY=37.497");
//         assertThat(requestUri.getQuery()).contains("EX=127.058");
//         assertThat(requestUri.getQuery()).contains("EY=37.511");
//         assertThat(requestUri.getQuery()).contains("OPT=0");
//         assertThat(requestUri.getQuery()).contains("SearchType=0");
//         assertThat(requestUri.getQuery()).contains("SearchPathType=0");
//         assertThat(requestUri.getQuery()).contains("apiKey=test-odsay-key");

//         assertThat(response.getResult().getSearchType()).isEqualTo(0);
//         assertThat(response.getResult().getBusCount()).isEqualTo(10);
//         assertThat(response.getResult().getPath()).hasSize(1);
//         assertThat(response.getResult().getPath().get(0).getPathType()).isEqualTo(1);
//         assertThat(response.getResult().getPath().get(0).getInfo().getTotalTime()).isEqualTo(37);
//         assertThat(response.getResult().getPath().get(0).getInfo().getTotalDistance()).isEqualTo(12500.0);
//         assertThat(response.getResult().getPath().get(0).getInfo().getFirstStartStation()).isEqualTo("강남역");
//     }

//     @Test
//     @DisplayName("ODsay 응답 본문에 error 필드가 있으면 인증 오류로 처리한다")
//     void getCommuteInfo_errorPayload_throwsUnauthorized() {
//         WebClient webClient = buildWebClient(request -> jsonResponse("""
//                 {
//                   "error": {
//                     "code": -1,
//                     "msg": "Invalid API Key"
//                   }
//                 }
//                 """));
//         OdsayTransitService service = new OdsayTransitService(webClient, new ObjectMapper());
//         ReflectionTestUtils.setField(service, "odsayApiKey", "bad-key");

//         assertThatThrownBy(() -> service.getCommuteInfo(buildRequest()))
//                 .isInstanceOf(BusinessException.class)
//                 .extracting("errorCode")
//                 .isEqualTo(ErrorCode.EXTERNAL_API_UNAUTHORIZED);
//     }

//     @Test
//     @DisplayName("ODsay HTTP 상태코드가 실패면 외부 API 오류로 처리한다")
//     void getCommuteInfo_httpError_throwsExternalApiError() {
//         WebClient webClient = buildWebClient(request ->
//                 Mono.just(ClientResponse.create(HttpStatus.BAD_GATEWAY)
//                         .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
//                         .body("{\"message\":\"temporary failure\"}")
//                         .build()));
//         OdsayTransitService service = new OdsayTransitService(webClient, new ObjectMapper());
//         ReflectionTestUtils.setField(service, "odsayApiKey", "test-odsay-key");

//         assertThatThrownBy(() -> service.getCommuteInfo(buildRequest()))
//                 .isInstanceOf(BusinessException.class)
//                 .extracting("errorCode")
//                 .isEqualTo(ErrorCode.EXTERNAL_API_ERROR);
//     }

//     private WebClient buildWebClient(ExchangeFunction exchangeFunction) {
//         return WebClient.builder()
//                 .exchangeFunction(exchangeFunction)
//                 .build();
//     }

//     private Mono<ClientResponse> jsonResponse(String body) {
//         return Mono.just(ClientResponse.create(HttpStatus.OK)
//                 .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
//                 .body(body)
//                 .build());
//     }

//     private CommuteRequest buildRequest() {
//         CommuteRequest request = new CommuteRequest();
//         request.setSX(127.033);
//         request.setSY(37.497);
//         request.setEX(127.058);
//         request.setEY(37.511);
//         request.setOpt(0);
//         request.setSearchType(0);
//         request.setSearchPathType(0);
//         return request;
//     }
// }
