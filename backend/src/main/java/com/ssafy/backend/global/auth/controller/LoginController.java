package com.ssafy.backend.global.auth.controller;

import com.ssafy.backend.global.auth.dto.request.GoogleLoginRequestDto;
import com.ssafy.backend.global.auth.dto.response.LoginAuthResponse;
import com.ssafy.backend.global.auth.service.GoogleLoginService;
import com.ssafy.backend.global.common.response.ApiResponse;
import com.ssafy.backend.global.error.code.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
public class LoginController {

    private final GoogleLoginService googleLoginService;

    @PostMapping("/auth")
    public ApiResponse<LoginAuthResponse> login(@Valid @RequestBody GoogleLoginRequestDto request) {
        return ApiResponse.success(SuccessCode.SUCCESS, googleLoginService.login(request.googleToken()));
    }
}
