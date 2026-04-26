package com.ssafy.backend.domain.statistics.controller;

import com.ssafy.backend.domain.statistics.service.StatisticsBackfillService;
import com.ssafy.backend.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class StatisticsAdminController {
    private final StatisticsBackfillService backfillService;

    @PostMapping("/backfill")
    public ApiResponse<String> startBackfill(
            @RequestParam String startMonth, 
            @RequestParam String endMonth) {
        backfillService.executeBackfill(startMonth, endMonth);
        return ApiResponse.success("백필 작업이 시작되었습니다. 로그를 확인하세요.");
    }
}
