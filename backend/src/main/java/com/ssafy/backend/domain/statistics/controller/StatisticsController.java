package com.ssafy.backend.domain.statistics.controller;

import com.ssafy.backend.domain.statistics.dto.response.RegionMonthlyStatisticResponse;
import com.ssafy.backend.domain.statistics.service.StatisticsScheduler;
import com.ssafy.backend.domain.statistics.service.StatisticsService;
import com.ssafy.backend.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 지역별 부동산 통계 조회 API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final StatisticsScheduler statisticsScheduler;

    /**
     * HDFS 데이터 적재를 수동으로 트리거합니다. (테스트용)
     */
    @PostMapping("/ingest")
    public String triggerIngest() {
        statisticsScheduler.scheduleMonthlyDataIngestion();
        return "Ingestion triggered! Check logs and HDFS.";
    }

    /**
     * 특정 지역의 월별 부동산 통계 추이(보증금, 월세, 거래량)를 조회합니다.
     *
     * @param sidoName    시/도 한글 명칭 (예: 서울특별시)
     * @param sigunguName 시/군/구 한글 명칭 (예: 종로구)
     * @return 월별 통계 데이터를 담은 공통 응답 객체
     */
    @GetMapping("/regions/monthly")
    public ApiResponse<RegionMonthlyStatisticResponse> getMonthlyRegionStatistics(
            @RequestParam("sidoName") String sidoName,
            @RequestParam("sigunguName") String sigunguName) {

        RegionMonthlyStatisticResponse response = statisticsService.getMonthlyRegionStatistics(sidoName, sigunguName);
        return ApiResponse.success(response);
    }
}
