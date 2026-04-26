package com.ssafy.backend.domain.commute.controller;

import com.ssafy.backend.domain.commute.dto.request.CommuteRequest;
import com.ssafy.backend.domain.commute.dto.response.OdsayResponse;
import com.ssafy.backend.domain.commute.service.OdsayTransitService;
import com.ssafy.backend.global.common.response.ApiResponse;
import com.ssafy.backend.global.error.code.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class CommuteController {

    private final OdsayTransitService odsayTransitService;

    @GetMapping("/commute")
    public ApiResponse<OdsayResponse> testCommute(
            @Valid @ModelAttribute CommuteRequest request
    ){
        return ApiResponse.success(
                SuccessCode.SUCCESS,
                odsayTransitService.getCommuteInfo(request)
        );
    }
}
