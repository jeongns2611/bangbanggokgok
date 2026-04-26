package com.ssafy.backend.domain.commute.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true) // 매핑되지 않은 필드는 무시한다.
@NoArgsConstructor
public class OdsayResponse {
    private Result result;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    public static class Result{
        private List<Path> path; // 추천 경로 리스트
        private Integer searchType; // 결과 구분 => 0 : 도시내/ 1 : 도시간 직통/ 2 : 도시간 환승
        private Integer pointDistance; // 출발지-도착지 직선 거리 (m단위)
        private Integer subwayCount; // 지하철 결과 개수
        private Integer busCount; // 버스 결과 개수
        private Integer subwayBusCount; // 버스 + 지하철 결과 개수
        private Integer outTrafficCheck; // 도시간 "직통" 탐색 결과 유무(환승 X)/ 0-False, 1-True
        private Integer startRadius; // 출발지 반경 NOTE: 필요 없을 경우 생략 가능
        private Integer endRadius; // 도착지 반경 NOTE: 필요 없을 경우 생략 가능
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    public static class Path{
        private Info info;
        private Integer pathType; // 결과 종류 ->  1 : 지하철 only/ 2 : 버스 only/ 3 : 버스 + 지하철
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    public static class Info{
        private int totalTime; // 총 소요시간
        private double totalDistance; // 총 거리
        private Integer payment; // 총 요금
        private Integer busTransitCount; // 버스 환승 카운트
        private Integer subwayTransitCount; // 지하철 환승 카운트
        private String firstStartStation; // 최초 출발역/정류장
        private String lastEndStation; // 최종 도착역/정류장
    }
}