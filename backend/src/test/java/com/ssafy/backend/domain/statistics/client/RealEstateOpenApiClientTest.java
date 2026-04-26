package com.ssafy.backend.domain.statistics.client;

import com.ssafy.backend.domain.statistics.dto.openapi.OfficetelRentItemDto;
import com.ssafy.backend.domain.statistics.dto.openapi.OpenApiResponseDto;
import com.ssafy.backend.domain.statistics.dto.openapi.SingleFamilyRentItemDto;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class RealEstateOpenApiClientTest {

    private MockWebServer mockWebServer;
    private RealEstateOpenApiClient apiClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(new XmlMapper(), MediaType.APPLICATION_XML));
                    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(new XmlMapper(), MediaType.APPLICATION_XML));
                }).build();

        WebClient webClient = WebClient.builder()
                .exchangeStrategies(strategies)
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        apiClient = new RealEstateOpenApiClient(webClient);
        ReflectionTestUtils.setField(apiClient, "serviceKey", "testServiceKey");
        ReflectionTestUtils.setField(apiClient, "baseUrl", mockWebServer.url("/").toString().replaceAll("/$", ""));
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("단독다가구 전월세 API 응답 파싱 테스트")
    void fetchSingleFamilyRent_ShouldParseXmlResponse() {
        // given
        String xmlResponse = "<response><header><resultCode>000</resultCode><resultMsg>OK</resultMsg></header>" +
                "<body><items><item>" +
                "<dealYear>2024</dealYear><dealMonth>7</dealMonth><dealDay>26</dealDay>" +
                "<deposit>10,000</deposit><monthlyRent>100</monthlyRent><sggCd>11110</sggCd>" +
                "<umdNm>청운동</umdNm><totalFloorAr>55</totalFloorAr><houseType>단독</houseType>" +
                "</item></items><numOfRows>1</numOfRows><pageNo>1</pageNo><totalCount>166</totalCount></body></response>";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .setBody(xmlResponse));

        // when
        OpenApiResponseDto<SingleFamilyRentItemDto> response = apiClient.fetchSingleFamilyRent("11110", "202407", 1).block();

        // then
        assertThat(response).isNotNull();
        assertThat(response.getHeader().getResultCode()).isEqualTo("000");
        assertThat(response.getBody().getItems()).hasSize(1);
        
        SingleFamilyRentItemDto item = response.getBody().getItems().get(0);
        assertThat(item.getDeposit()).isEqualTo("10,000");
        assertThat(item.getDealYear()).isEqualTo(2024);
        assertThat(item.getSggCd()).isEqualTo("11110");
        assertThat(item.getMonthlyRent()).isEqualTo("100");
    }

    @Test
    @DisplayName("오피스텔 전월세 API 응답 파싱 테스트")
    void fetchOfficetelRent_ShouldParseXmlResponse() {
        // given
        String xmlResponse = "<response><header><resultCode>000</resultCode><resultMsg>OK</resultMsg></header>" +
                "<body><items><item>" +
                "<buildYear>2022</buildYear><dealYear>2024</dealYear><dealMonth>7</dealMonth><dealDay>31</dealDay>" +
                "<deposit>19,400</deposit><excluUseAr>21.14</excluUseAr><floor>13</floor><jibun>1425</jibun>" +
                "<monthlyRent>0</monthlyRent><offiNm>한라 운종가</offiNm><sggCd>11110</sggCd><sggNm>종로구</sggNm><umdNm>숭인동</umdNm>" +
                "</item></items><numOfRows>1</numOfRows><pageNo>1</pageNo><totalCount>138</totalCount></body></response>";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .setBody(xmlResponse));

        // when
        OpenApiResponseDto<OfficetelRentItemDto> response = apiClient.fetchOfficetelRent("11110", "202407", 1).block();

        // then
        assertThat(response).isNotNull();
        assertThat(response.getHeader().getResultCode()).isEqualTo("000");
        assertThat(response.getBody().getItems()).hasSize(1);
        
        OfficetelRentItemDto item = response.getBody().getItems().get(0);
        assertThat(item.getDeposit()).isEqualTo("19,400");
        assertThat(item.getOffiNm()).isEqualTo("한라 운종가");
        assertThat(item.getFloor()).isEqualTo(13);
        assertThat(item.getDealMonth()).isEqualTo(7);
    }

    @Test
    @DisplayName("API 호출 실패(4xx 에러)시 BusinessException(EXTERNAL_API_ERROR)을 반환해야 한다")
    void fetchRent_ShouldThrowBusinessException_On4xxError() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("Unauthorized"));

        // when & then
        try {
            apiClient.fetchSingleFamilyRent("11110", "202407", 1).block();
        } catch (BusinessException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.EXTERNAL_API_ERROR);
            assertThat(e.getMessage()).contains("인증 및 파라미터 오류");
        }
    }

    @Test
    @DisplayName("API 응답이 불일치하는 경우, 파싱 예외를 잡아 BusinessException(EXTERNAL_API_ERROR)을 반환해야 한다")
    void fetchRent_ShouldThrowBusinessException_OnParsingError() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"error\":\"Unexpected format\"}"));

        // when & then
        try {
            apiClient.fetchOfficetelRent("11110", "202407", 1).block();
        } catch (BusinessException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.EXTERNAL_API_ERROR);
            assertThat(e.getMessage()).contains("연동 중 오류");
        }
    }
}
