package com.ssafy.backend.domain.commute.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.domain.commute.dto.request.CommuteRequest;
import com.ssafy.backend.domain.commute.dto.response.OdsayResponse;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class OdsayTransitService {
    private static final double SHORT_DISTANCE_THRESHOLD_METERS = 800.0;
    private static final int SHORT_DISTANCE_FIXED_TIME_MINUTES = 5;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ApiCallService apiCallService;

    @Value("${odsay.api.key}")
    private String odsayApiKey;

    /**
     * 외부 Odsay API를 호출해서 길찾기 정보를 가져온다.
     */
    // TODO Cacheable, CircuitBreaker, Fallback 로직 도입
    public OdsayResponse getCommuteInfo(CommuteRequest request) {
        if (isWithinShortDistance(request)) {
            return generateShortDistanceResponse(request);
        }

        if (apiCallService.isLimitReached()) {
            return generateMockResponse(request);
        }

        log.info(
                "ODsay API 호출 시작 - 출발: {}, {}/ 도착: {}, {} / opt: {}, searchType: {}, searchPathType: {}",
                request.getSX(),
                request.getSY(),
                request.getEX(),
                request.getEY(),
                request.getOpt(),
                request.getSearchType(),
                request.getSearchPathType()
        );
        log.info("ODsay API 키 확인: {}", maskApiKey(odsayApiKey));
        try {
            URI requestUri = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host("api.odsay.com")
                    .path("/v1/api/searchPubTransPathT")
                    .queryParam("SX", request.getSX())
                    .queryParam("SY", request.getSY())
                    .queryParam("EX", request.getEX())
                    .queryParam("EY", request.getEY())
                    .queryParam("OPT", request.getOpt())
                    .queryParam("SearchType", request.getSearchType())
                    .queryParam("SearchPathType", request.getSearchPathType())
                    // ODsay 샘플과 동일하게 apiKey는 UTF-8로 명시 인코딩한다.
                    .queryParam("apiKey", UriUtils.encodeQueryParam(odsayApiKey, StandardCharsets.UTF_8))
                    .build(true)
                    .toUri();

            String rawJson = webClient.get()
                    .uri(requestUri)
                    .header("Referer", "https://j14a104.p.ssafy.io") // ODsay Web 인증을 위한 Referer 추가
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .map(body -> {
                                        log.error("ODsay API 오류 응답 - status: {}, body: {}", clientResponse.statusCode(), body);
                                        return new BusinessException(
                                                ErrorCode.EXTERNAL_API_ERROR,
                                                "ODsay API 호출 실패: " + clientResponse.statusCode()
                                        );
                                    }))
                    .bodyToMono(String.class)
                    .block();

            log.info("ODsay에서 온 날것의 JSON: {}", rawJson);
            JsonNode root = objectMapper.readTree(rawJson);
            if (root.has("error")) {
                throw new BusinessException(
                        ErrorCode.EXTERNAL_API_UNAUTHORIZED,
                        "ODsay API 응답 오류: " + root.get("error")
                );
            }

            // 실호출 성공 시 횟수 증가
            apiCallService.incrementCallCount();

            return objectMapper.treeToValue(root, OdsayResponse.class);
        } catch (WebClientResponseException e) {
            log.error("ODsay API 호출 실패 - status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BusinessException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    "ODsay API 호출 실패 - status: " + e.getStatusCode()
            );
        } catch (BusinessException e) {
            throw e;
        } catch (JsonProcessingException e) {
            log.error("ODsay API 응답 파싱 실패", e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "ODsay API 응답 파싱 실패");
        }
    }

    private OdsayResponse generateMockResponse(CommuteRequest request) {
        log.info("ODsay API 호출 제한 도달(하루 500회) - 임의 데이터 반환");
        OdsayResponse response = new OdsayResponse();
        OdsayResponse.Result result = new OdsayResponse.Result();

        OdsayResponse.Path path = new OdsayResponse.Path();
        OdsayResponse.Info info = new OdsayResponse.Info();

        // 30~60분 사이의 임의 시간 부여
        int randomTime = 30 + (int) (Math.random() * 30);
        info.setTotalTime(randomTime);
        info.setTotalDistance(5.0 + (Math.random() * 10.0));
        info.setPayment(1250 + (int) (Math.random() * 1000));
        info.setBusTransitCount(1);
        info.setSubwayTransitCount(1);
        info.setFirstStartStation("임의 출발 정류장");
        info.setLastEndStation("임의 도착 정류장");

        path.setInfo(info);
        path.setPathType(3); // 버스+지하철 혼합

        result.setPath(java.util.List.of(path));
        result.setSearchType(0);
        result.setSubwayBusCount(1);

        response.setResult(result);
        return response;
    }

    private OdsayResponse generateShortDistanceResponse(CommuteRequest request) {
        double distanceMeters = calculateDistanceMeters(request);
        log.info("ODsay API 호출 생략 - 직선거리 {}m 이내", Math.round(distanceMeters));

        OdsayResponse response = new OdsayResponse();
        OdsayResponse.Result result = new OdsayResponse.Result();
        OdsayResponse.Path path = new OdsayResponse.Path();
        OdsayResponse.Info info = new OdsayResponse.Info();

        info.setTotalTime(SHORT_DISTANCE_FIXED_TIME_MINUTES);
        info.setTotalDistance(distanceMeters);
        info.setPayment(0);
        info.setBusTransitCount(0);
        info.setSubwayTransitCount(0);
        info.setFirstStartStation("근거리 출발");
        info.setLastEndStation("근거리 도착");

        path.setInfo(info);
        path.setPathType(3);

        result.setPath(java.util.List.of(path));
        result.setSearchType(0);
        result.setPointDistance((int) Math.round(distanceMeters));
        result.setSubwayBusCount(1);

        response.setResult(result);
        return response;
    }

    private boolean isWithinShortDistance(CommuteRequest request) {
        return calculateDistanceMeters(request) <= SHORT_DISTANCE_THRESHOLD_METERS;
    }

    private double calculateDistanceMeters(CommuteRequest request) {
        double startLatRad = Math.toRadians(request.getSY());
        double endLatRad = Math.toRadians(request.getEY());
        double deltaLat = Math.toRadians(request.getEY() - request.getSY());
        double deltaLng = Math.toRadians(request.getEX() - request.getSX());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(startLatRad) * Math.cos(endLatRad)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6_371_000 * c;
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return "null-or-empty";
        }
        if (apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
