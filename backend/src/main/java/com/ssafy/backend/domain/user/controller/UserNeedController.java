package com.ssafy.backend.domain.user.controller;

import com.ssafy.backend.domain.user.dto.request.UserNeedRequestDto;
import com.ssafy.backend.domain.user.dto.response.*;
import com.ssafy.backend.domain.user.service.UserNeedService;
import com.ssafy.backend.global.auth.principal.CustomOAuth2User;
import com.ssafy.backend.global.common.response.ApiResponse;
import com.ssafy.backend.global.error.code.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-needs")
@RequiredArgsConstructor
public class UserNeedController {

    private final UserNeedService userNeedService;

    @GetMapping({"", "/"})
    public ApiResponse<UserNeedListResponseDto> getUserNeeds(
            @AuthenticationPrincipal CustomOAuth2User user
    ) {
        return ApiResponse.success(userNeedService.getUserNeeds(user.getUserId()));
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<UserNeedDetailResponseDto> getUserNeedDetail(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable Long id
    ) {
        return ApiResponse.success(userNeedService.getUserNeedDetail(user.getUserId(), id));
    }

    @PostMapping
    public ApiResponse<UserNeedCreateResponseDto> createUserNeed(
            @AuthenticationPrincipal CustomOAuth2User user,
            @Valid @RequestBody UserNeedRequestDto request
    ) {
        return ApiResponse.success(SuccessCode.CREATED, userNeedService.createUserNeed(user.getUserId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserNeedUpdateResponseDto> updateUserNeed(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable Long id,
            @Valid @RequestBody UserNeedRequestDto request
    ) {
        return ApiResponse.success(SuccessCode.UPDATED, userNeedService.updateUserNeed(user.getUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUserNeed(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable Long id
    ) {
        userNeedService.deleteUserNeed(user.getUserId(), id);
        return ApiResponse.success(SuccessCode.DELETED);
    }

    @PutMapping("/set-default/{id}")
    public ApiResponse<UserNeedDefaultResponseDto> setDefaultUserNeed(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable Long id
    ) {
        return ApiResponse.success(
                SuccessCode.UPDATED,
                userNeedService.setUserNeedDefault(user.getUserId(), id)
        );
    }
}
