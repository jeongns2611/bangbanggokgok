package com.ssafy.backend.domain.statistics.client;

import com.ssafy.backend.domain.statistics.dto.openapi.OfficetelRentItemDto;
import com.ssafy.backend.domain.statistics.dto.openapi.OpenApiResponseDto;
import com.ssafy.backend.domain.statistics.dto.openapi.SingleFamilyRentItemDto;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * 국토교통부 공공데이터 포털의 부동산 실거래가 Open API와 통신하는 클라이언트 컴포넌트입니다.
 * WebClient를 사용하여 비동기/논블로킹 방식으로 데이터를 요청하며, XML 응답을 Java 객체로 파싱합니다.
 */
@Slf4j
@Component
public class RealEstateOpenApiClient {

    @Value("${openapi.real-estate.base-url:http://apis.data.go.kr}")
    private String baseUrl;

    @Value("${openapi.real-estate.key}")
    private String serviceKey;

    private final WebClient webClient;

    /**
     * WebClient 주입 및 XML 파싱 설정을 위한 생성자입니다.
     * 공공데이터 포털의 일부 API가 'application/xml;charset=UTF-8'과 같이 비표준 Content-Type을 반환하므로,
     * 로컬 WebClient 인스턴스를 mutate하여 Jackson XML 디코더를 추가로 등록합니다.
     *
     * @param webClient 전역 설정이 적용된 WebClient.Builder로부터 생성된 빈
     */
    public RealEstateOpenApiClient(WebClient webClient) {
        // 전역 빈 대신 로컬 객체에 XML 파서(Jackson)를 추가합니다. 
        // 포털이 charset=utf-8을 내려주므로 MediaType 매칭을 넓게 가져갑니다.
        this.webClient = webClient.mutate()
                .codecs(configurer -> {
                    // 응답 데이터가 클 경우를 대비해 인메모리 버퍼 사이즈 확장
                    configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024);
                    // XML 데이터를 Jackson XmlMapper를 사용해 디코딩하도록 설정
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(new XmlMapper(), MediaType.APPLICATION_XML, new MediaType("application", "xml"))
                    );
                })
                .build();
    }

    /**
     * 단독/다가구 전월세 실거래가 자료 조회
     *
     * @param lawdCd  지역코드 (5자리)
     * @param dealYmd 계약월 (YYYYMM)
     * @param pageNo  페이지번호
     * @return Mono<OpenApiResponseDto < SingleFamilyRentItemDto>>
     */
    public Mono<OpenApiResponseDto<SingleFamilyRentItemDto>> fetchSingleFamilyRent(String lawdCd, String dealYmd, int pageNo) {
        // 공공데이터 포털의 Service Key는 이미 인코딩되어 있는 경우가 많으므로, 
        // UriComponentsBuilder에서 이중 인코딩되지 않도록 build(true)를 사용합니다.
        URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/1613000/RTMSDataSvcSHRent/getRTMSDataSvcSHRent")
                .queryParam("serviceKey", serviceKey)
                .queryParam("LAWD_CD", lawdCd)
                .queryParam("DEAL_YMD", dealYmd)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", 100)
                .build(true) // 인코딩된 키의 이중 인코딩 방지
                .toUri();

        return webClient.get()
                .uri(uri)
                .retrieve()
                // HTTP 상태 코드별 예외 처리
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "공공데이터 인증 및 파라미터 오류가 발생했습니다.")))
                .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "공공데이터 서버 오류가 발생했습니다.")))
                .bodyToMono(new ParameterizedTypeReference<OpenApiResponseDto<SingleFamilyRentItemDto>>() {})
                .onErrorMap(e -> {
                    if (e instanceof BusinessException) return e;
                    log.error("단독다가구 API 파싱/네트워크 접속 에러: {}", e.getMessage());
                    return new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "공공데이터 연동 중 오류가 발생했습니다.");
                });
    }

    /**
     * 오피스텔 전월세 실거래가 자료 조회
     *
     * @param lawdCd  지역코드 (5자리)
     * @param dealYmd 계약월 (YYYYMM)
     * @param pageNo  페이지번호
     * @return Mono<OpenApiResponseDto < OfficetelRentItemDto>>
     */
    public Mono<OpenApiResponseDto<OfficetelRentItemDto>> fetchOfficetelRent(String lawdCd, String dealYmd, int pageNo) {
        // 오피스텔 API 호출을 위한 URI 구성 및 이중 인코딩 방지 처리
        URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/1613000/RTMSDataSvcOffiRent/getRTMSDataSvcOffiRent")
                .queryParam("serviceKey", serviceKey)
                .queryParam("LAWD_CD", lawdCd)
                .queryParam("DEAL_YMD", dealYmd)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", 100)
                .build(true) // 인코딩된 키의 이중 인코딩 방지
                .toUri();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "공공데이터 인증 및 파라미터 오류가 발생했습니다.")))
                .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "공공데이터 서버 오류가 발생했습니다.")))
                .bodyToMono(new ParameterizedTypeReference<OpenApiResponseDto<OfficetelRentItemDto>>() {})
                .onErrorMap(e -> {
                    if (e instanceof BusinessException) return e;
                    log.error("오피스텔 API 파싱/네트워크 접속 에러: {}", e.getMessage());
                    return new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "공공데이터 연동 중 오류가 발생했습니다.");
                });
    }
}
