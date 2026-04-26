package com.ssafy.backend.domain.statistics.controller;

import com.ssafy.backend.domain.statistics.dto.request.RegionInfraStatisticsRequest;
import com.ssafy.backend.domain.statistics.dto.response.RegionInfraStatisticsResponse;
import com.ssafy.backend.domain.statistics.service.RegionInfraStatisticsService;
import com.ssafy.backend.global.common.response.ApiResponse;
import com.ssafy.backend.global.error.code.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics/regions")
@RequiredArgsConstructor
/**
 * 지역(시/도 + 시/군/구) 기준 생활 통계 조회 API
 */
public class RegionInfraStatisticsController {

    private final RegionInfraStatisticsService regionInfraStatisticsService;

    /**
     * 생활비 지수 + 생활 인프라 + 지역 안전도를 통합 조회
     */
    @GetMapping("/infra")
    public ApiResponse<RegionInfraStatisticsResponse> getRegionInfraStatistics(
            @Valid @ModelAttribute RegionInfraStatisticsRequest request
    ) {
        return ApiResponse.success(
                SuccessCode.SUCCESS,
                regionInfraStatisticsService.getRegionInfraStatistics(request)
        );
    }
}
