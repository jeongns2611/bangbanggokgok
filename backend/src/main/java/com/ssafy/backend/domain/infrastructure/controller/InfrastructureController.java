package com.ssafy.backend.domain.infrastructure.controller;

import com.ssafy.backend.domain.infrastructure.dto.response.NearbyInfrastructureResponse;
import com.ssafy.backend.domain.infrastructure.service.InfrastructureService;
import com.ssafy.backend.global.common.response.ApiResponse;
import com.ssafy.backend.global.error.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/house")
@RequiredArgsConstructor
public class InfrastructureController {

    private final InfrastructureService infrastructureService;

    /**
     * 매물 주변 인프라 조회 API
     *  - 특정 매물 기준 반경 800m 내 인프라 요약 및 집계 정보 반환
     */
    @GetMapping("/{houseId}/infrastructures")
    public ApiResponse<NearbyInfrastructureResponse> getNearbyInfrastructures(@PathVariable Long houseId) {
        NearbyInfrastructureResponse response = infrastructureService.getNearbyInfrastructures(houseId);
        return ApiResponse.success(SuccessCode.SUCCESS, response);
    }
}

