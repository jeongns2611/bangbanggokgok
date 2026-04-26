package com.ssafy.backend.domain.statistics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.domain.region.entity.RegionSigungu;
import com.ssafy.backend.domain.region.repository.RegionSigunguRepository;
import com.ssafy.backend.domain.statistics.client.HdfsClient;
import com.ssafy.backend.domain.statistics.client.RealEstateOpenApiClient;
import com.ssafy.backend.domain.statistics.dto.openapi.OfficetelRentItemDto;
import com.ssafy.backend.domain.statistics.dto.openapi.OpenApiResponseDto;
import com.ssafy.backend.domain.statistics.dto.openapi.SingleFamilyRentItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 매일 정기적으로 공공데이터 API를 호출하여 HDFS에 원천 데이터를 적재하는 스케줄러입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsScheduler {

    private final RealEstateOpenApiClient openApiClient;
    private final HdfsClient hdfsClient;
    private final ObjectMapper objectMapper;
    private final RegionSigunguRepository regionSigunguRepository;

    /**
     * 매일 새벽 4시에 실행 (0 0 4 * * ?)
     * DB에 등록된 모든 시군구의 전월 및 당월 데이터를 수집하여 HDFS에 저장합니다.
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void scheduleMonthlyDataIngestion() {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        log.info("정기 데이터 수집 스케줄러 시작: {}", currentMonth);

        // DB에서 모든 시군구 정보를 가져옴
        log.info("전체 시군구 조회 테스트 시작...");
        log.info("전체 시군구 수: {}", regionSigunguRepository.count());

        // 서울시(11) 코드에 해당하는 시군구 정보만 가져옴
        List<RegionSigungu> sigungus = regionSigunguRepository.findByRegionSidoSidoCode("11");
        log.info("수집 대상 지역 수 (서울): {}", sigungus.size());

        for (RegionSigungu sigungu : sigungus) {
            String lawdCd = sigungu.getSigunguCode();
            
            // 1. 단독다가구 데이터 수집 및 적재
            ingestSingleFamilyRent(lawdCd, currentMonth);
            
            // 2. 오피스텔 데이터 수집 및 적재
            ingestOfficetelRent(lawdCd, currentMonth);
        }
    }

    private void ingestSingleFamilyRent(String lawdCd, String dealYmd) {
        openApiClient.fetchSingleFamilyRent(lawdCd, dealYmd, 1)
                .flatMap(response -> {
                    if (response.getBody() == null || response.getBody().getItems() == null) {
                        return Mono.empty();
                    }
                    return saveToHdfs("single-family", lawdCd, dealYmd, response.getBody().getItems());
                })
                .subscribe();
    }

    private void ingestOfficetelRent(String lawdCd, String dealYmd) {
        openApiClient.fetchOfficetelRent(lawdCd, dealYmd, 1)
                .flatMap(response -> {
                    if (response.getBody() == null || response.getBody().getItems() == null) {
                        return Mono.empty();
                    }
                    return saveToHdfs("officetel", lawdCd, dealYmd, response.getBody().getItems());
                })
                .subscribe();
    }

    private <T> Mono<Void> saveToHdfs(String type, String lawdCd, String dealYmd, List<T> items) {
        try {
            String jsonData = objectMapper.writeValueAsString(items);
            String year = dealYmd.substring(0, 4);
            String month = dealYmd.substring(4, 6);
            
            // 경로 예시: /raw/statistics/officetel/2024/07/11110_data.json
            String hdfsPath = String.format("/raw/statistics/%s/%s/%s/%s_data.json", 
                    type, year, month, lawdCd);
            
            return hdfsClient.writeJsonFile(hdfsPath, jsonData);
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패: {}", e.getMessage());
            return Mono.error(e);
        }
    }
}
