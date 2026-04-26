package com.ssafy.backend.domain.statistics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.domain.region.repository.RegionSigunguRepository;
import com.ssafy.backend.domain.statistics.client.HdfsClient;
import com.ssafy.backend.domain.statistics.client.RealEstateOpenApiClient;
import com.ssafy.backend.domain.region.entity.RegionSigungu;
import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;

/**
 * 과거 부동산 실거래가 데이터를 일괄 수집하여 HDFS에 적재하는 백필 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsBackfillService {
    private final RealEstateOpenApiClient openApiClient;
    private final HdfsClient hdfsClient;
    private final RegionSigunguRepository regionSigunguRepository;
    private final ObjectMapper objectMapper;

    /**
     * 지정된 기간(시작월 ~ 종료월) 동안의 모든 서울시 시군구 데이터를 수집합니다.
     * 
     * @param startMonth 시작 월 (포맷: YYYYMM)
     * @param endMonth   종료 월 (포맷: YYYYMM)
     */
    public void executeBackfill(String startMonth, String endMonth) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        YearMonth start = YearMonth.parse(startMonth, formatter);
        YearMonth end = YearMonth.parse(endMonth, formatter);

        // 1. 수집해야 할 월 리스트 동적 생성 (202501, 202502 ...)
        List<String> months = new ArrayList<>();
        YearMonth temp = start;
        while (!temp.isAfter(end)) {
            months.add(temp.format(formatter));
            temp = temp.plusMonths(1);
        }

        // 서울시(11) 코드에 해당하는 모든 시군구 조회
        List<RegionSigungu> sigungus = regionSigunguRepository.findByRegionSidoSidoCode("11"); 
        
        log.info(">>> 백필 작업 시작: {} ~ {} (총 {}개월, {}개 지역)", 
                startMonth, endMonth, months.size(), sigungus.size());

        // 2. 리액티브 파이프라인 구성: 월별 -> 지역별 순차적 수집
        Flux.fromIterable(months)
            .concatMap(month -> Flux.fromIterable(sigungus)
                // 공공데이터 API 호출 제한(Throttling)을 고려하여 300ms 지연 추가
                .delayElements(Duration.ofMillis(300)) 
                .flatMap(sigungu -> {
                    String lawdCd = sigungu.getSigunguCode();
                    log.info("[{}] 수집 시도 - 지역: {}, 코드: {}", month, sigungu.getSigunguName(), lawdCd);
                    
                    // 단독다가구와 오피스텔 데이터를 동시에 수집하고 HDFS에 저장
                    return Mono.zip(
                        ingestRent("single-family", lawdCd, month),
                        ingestRent("officetel", lawdCd, month)
                    );
                })
            )
            .subscribe(
                result -> {}, // 온전한 처리 시 별도 액션 없음
                error -> log.error("!!! 백필 도중 치명적 에러 발생: {}", error.getMessage()),
                () -> log.info(">>> 모든 백필 작업이 성공적으로 완료되었습니다.")
            );
    }

    /**
     * 특정 부동산 타입 및 지역의 거래 데이터를 API로부터 가져와 HDFS에 저장합니다.
     */
    private Mono<Void> ingestRent(String type, String lawdCd, String dealYmd) {
        if (type.equals("single-family")) {
            return openApiClient.fetchSingleFamilyRent(lawdCd, dealYmd, 1)
                    .flatMap(response -> {
                        if (response.getBody() == null || response.getBody().getItems() == null) {
                            return Mono.empty();
                        }
                        return saveToHdfs(type, lawdCd, dealYmd, response.getBody().getItems());
                    });
        } else {
            return openApiClient.fetchOfficetelRent(lawdCd, dealYmd, 1)
                    .flatMap(response -> {
                        if (response.getBody() == null || response.getBody().getItems() == null) {
                            return Mono.empty();
                        }
                        return saveToHdfs(type, lawdCd, dealYmd, response.getBody().getItems());
                    });
        }
    }

    /**
     * 데이터를 JSON으로 직렬화하여 HDFS의 지정된 경로에 저장합니다.
     */
    private <T> Mono<Void> saveToHdfs(String type, String lawdCd, String dealYmd, List<T> items) {
        try {
            String jsonData = objectMapper.writeValueAsString(items);
            String year = dealYmd.substring(0, 4);
            String month = dealYmd.substring(4, 6);
            
            // HDFS 계층 구조에 따른 최종 경로 생성
            String hdfsPath = String.format("/raw/statistics/%s/%s/%s/%s_data.json", 
                    type, year, month, lawdCd);
            
            return hdfsClient.writeJsonFile(hdfsPath, jsonData);
        } catch (JsonProcessingException e) {
            log.error("JSON 포맷팅 실패: {}", e.getMessage());
            return Mono.error(e);
        }
    }
}