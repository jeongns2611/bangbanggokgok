package com.ssafy.backend.domain.house.service;

import com.ssafy.backend.domain.house.dto.response.KakaoAddressResponse;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class KakaoGeoService {
    private static final String KAKAO_BASE_URL = "https://dapi.kakao.com";
    private final RestClient restClient;

    public KakaoGeoService(@Value("${kakao.api.key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(KAKAO_BASE_URL) // 도메인까지만 설정
                .defaultHeader("Authorization", "KakaoAK " + apiKey)
                .defaultHeader("Content-Type", "application/json;charset=UTF-8") // 인코딩 명시
                .build();
    }

    public KakaoAddressResponse.Document getCoordinates(String address) {
        if (address == null || address.isBlank()) {
            log.info("KakaoGeoService.getCoordinates called with blank address");
            throw new BusinessException(ErrorCode.FAIL, "주소가 입력되지 않았습니다.");
        }

        String trimmed = address.trim();
        log.info("Kakao API request - query={}", trimmed);

        KakaoAddressResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/address.json")
                        .queryParam("query", trimmed)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response1) -> {
                    log.info("카카오 API 호출 실패: {} {} {}", response1.getStatusCode(), response1.getStatusText(), response1.getBody());
                    throw new BusinessException(ErrorCode.FAIL, "카카오 API 호출 실패: " + response1.getStatusCode());
                })
                .body(KakaoAddressResponse.class);

        if (response == null || response.documents() == null || response.documents().isEmpty()) {
            log.info("좌표를 찾을 수 없는 주소입니다: {}", trimmed);
            throw new BusinessException(ErrorCode.FAIL, "좌표를 찾을 수 없는 주소입니다: " + trimmed);
        }

        KakaoAddressResponse.Document doc = response.documents().get(0);
        log.info("Kakao API response - query={}, x={}, y={}", trimmed, doc.x(), doc.y());
        return doc;
    }
}
