package com.ssafy.backend.domain.house.controller;

import com.ssafy.backend.domain.ai.dto.response.HouseCompareAiResponse;
import com.ssafy.backend.domain.ai.service.HouseCompareAiService;
import com.ssafy.backend.domain.house.dto.request.*;
import com.ssafy.backend.domain.house.dto.response.*;
import com.ssafy.backend.domain.house.service.CurrentHouseService;
import com.ssafy.backend.domain.house.service.HouseCompareService;
import com.ssafy.backend.domain.house.service.S3Service;
import com.ssafy.backend.global.auth.principal.CustomOAuth2User;
import com.ssafy.backend.global.common.response.ApiResponse;
import com.ssafy.backend.global.error.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/houses")
@RequiredArgsConstructor
public class CurrentHouseController {
    private final CurrentHouseService currentHouseService;
    private final HouseCompareService houseCompareService;
    private final HouseCompareAiService houseCompareAiService;
    private final S3Service s3Service;

    @PostMapping("/address/validate")
    public ApiResponse<?> validateAddress(@RequestBody AddressValidRequest req) {
        return ApiResponse.success(SuccessCode.SUCCESS, currentHouseService.validateAddress(req.getAddress()));
    }

    @PostMapping
    public ApiResponse<?> create(
            @RequestBody CurrentHouseRequest req,
            @AuthenticationPrincipal CustomOAuth2User userDetails
    ) {
        HouseCreateResponse response = currentHouseService.create(req, userDetails == null ? 1L : userDetails.getUserId());
        return ApiResponse.success(SuccessCode.CREATED, response);
    }

    @PostMapping("/list")
    public ApiResponse<?> createList(
            @RequestBody List<CurrentHouseRequest> reqs,
            @AuthenticationPrincipal CustomOAuth2User userDetails
    ) {
        Long userId = userDetails == null ? 1L : userDetails.getUserId();

        // 요청받은 리스트를 순회하며 개별 등록 로직을 수행하고, 결과를 리스트로 모읍니다.
        List<HouseCreateResponse> responses = reqs.stream()
                .map(req -> currentHouseService.create(req, userId))
                .toList();

        return ApiResponse.success(SuccessCode.CREATED, responses);
    }

    @GetMapping("/{houseId}")
    public ApiResponse<?> getById(@PathVariable Long houseId, @AuthenticationPrincipal CustomOAuth2User userDetails) {
        CurrentHouseResponse response = currentHouseService.findById(houseId);
        Long userId = userDetails == null ? 1L : userDetails.getUserId();
        response.setIsLiked(currentHouseService.checkLiked(houseId, userId));

        return ApiResponse.success(SuccessCode.SUCCESS, response);
    }

    // 현재 findByIdWithStats 에서 인프라 통계(개수/최단거리)를 함께 조립해 반환한다.
    @GetMapping("/search/{houseId}")
    public ApiResponse<?> getByIdWithStats(@PathVariable Long houseId, @AuthenticationPrincipal CustomOAuth2User userDetails) {
        CurrentHouseSearchResponse response = currentHouseService.findByIdWithStats(houseId);
        Long userId = userDetails == null ? 1L : userDetails.getUserId();
        response.setIsLiked(currentHouseService.checkLiked(houseId, userId));
        return ApiResponse.success(SuccessCode.SUCCESS, response);
    }

    @GetMapping("/compare")
    public ApiResponse<?> compareHouses(
            @RequestParam String ids,
            @AuthenticationPrincipal CustomOAuth2User userDetails
    ) {
        HouseCompareResponse response = houseCompareService.compare(
                new HouseCompareRequest(ids).ids(),
                userDetails == null ? null : userDetails.getUserId()
        );
        return ApiResponse.success(SuccessCode.SUCCESS, response);
    }

    @PostMapping("/compare/ai")
    public ApiResponse<?> compareHousesByAi(
            @RequestBody HouseCompareAiRequest compareRequest,
            @AuthenticationPrincipal CustomOAuth2User userDetails
    ) {
        HouseCompareAiResponse response = houseCompareAiService.compare(
                compareRequest,
                userDetails == null ? null : userDetails.getUserId()
        );
        return ApiResponse.success(SuccessCode.SUCCESS, response);
    }

    @GetMapping
    public ApiResponse<?> getAll(@ModelAttribute HousePageable pageable, @AuthenticationPrincipal CustomOAuth2User userDetails) {
        Long userId = userDetails == null ? 1L : userDetails.getUserId();
        CurrentHouseListResponse response = currentHouseService.findAll(userId, pageable);
        List<Long> houseIds = response.getData().stream()
                .map(CurrentHouseListResponse.ListElement::getHouseId)
                .toList();
        Map<Long, Boolean> likedMap = currentHouseService.checkLikedList(userId, houseIds);
        response.getData().forEach(house -> house.setIsLiked(likedMap.getOrDefault(house.getHouseId(), false)));

        return ApiResponse.success(response);
    }

    @GetMapping("/search")
    public ApiResponse<?> getAllByFilter(@RequestBody MapSearchRequest req, @AuthenticationPrincipal CustomOAuth2User userDetails) {
        Long userId = userDetails == null ? 1L : userDetails.getUserId();
        CurrentHouseListScrollResponse response = currentHouseService.findAllByFilterCondition(req);

        List<Long> houseIds = response.getData().stream()
                .map(CurrentHouseListResponse.ListElement::getHouseId)
                .toList();
        Map<Long, Boolean> likedMap = currentHouseService.checkLikedList(userId, houseIds);
        response.getData().forEach(house -> house.setIsLiked(likedMap.getOrDefault(house.getHouseId(), false)));

        return ApiResponse.success(response);
    }

    @PutMapping("/{houseId}")
    public ApiResponse<?> update(@PathVariable Long houseId, @RequestBody CurrentHouseRequest req, @AuthenticationPrincipal CustomOAuth2User userDetails) {
        CurrentHouseResponse response = currentHouseService.update(houseId, req);
        Long userId = userDetails == null ? 1L : userDetails.getUserId();
        response.setIsLiked(currentHouseService.checkLiked(houseId, userId));

        return ApiResponse.success(SuccessCode.UPDATED, response);
    }


    @DeleteMapping("/{houseId}")
    public ApiResponse<?> delete(@PathVariable Long houseId, @AuthenticationPrincipal CustomOAuth2User userDetails) {
        Long userId = userDetails == null ? 1L : userDetails.getUserId();
        CurrentHouseResponse response = currentHouseService.delete(houseId, userId);
        response.setIsLiked(currentHouseService.checkLiked(houseId, userId));

        return ApiResponse.success(SuccessCode.DELETED, response);
    }

    @PostMapping("/{houseId}/sold")
    public ApiResponse<?> sold(@PathVariable Long houseId, @AuthenticationPrincipal CustomOAuth2User userDetails) {
        Long userId = userDetails == null ? 1L : userDetails.getUserId();
        CurrentHouseResponse response = currentHouseService.sold(houseId);
        response.setIsLiked(currentHouseService.checkLiked(houseId, userId));

        return ApiResponse.success(SuccessCode.UPDATED, response);
    }

    @GetMapping("/wish")
    public ApiResponse<?> getWishList(@ModelAttribute HousePageable pageable, @AuthenticationPrincipal CustomOAuth2User userDetails) {
        CurrentHouseListResponse response = currentHouseService.findWishList(userDetails == null ? 1L : userDetails.getUserId(), pageable);
        response.getData().forEach(house -> house.setIsLiked(true));
        return ApiResponse.success(SuccessCode.SUCCESS, response);
    }

    @PostMapping("/wish/{houseId}")
    public ApiResponse<?> toggleWishList(@PathVariable Long houseId, @AuthenticationPrincipal CustomOAuth2User userDetails) {
        Boolean result = currentHouseService.toggleWishList(houseId, userDetails == null ? 1L : userDetails.getUserId());
        return ApiResponse.success(SuccessCode.UPDATED, new ToggleWishListResponse(result, result ? "매물 찜하기 성공" : "매물 찜하기 삭제 성공"));
    }

    @PostMapping("/presigned-url")
    public ApiResponse<?> createPresignedUrl(@RequestBody List<UploadRequest> requests) {
        List<PresignedUrlResponse> response = requests.stream()
                .map(req -> s3Service.getPresignedUrl(req.houseId(), req.fileName(), req.contentType(), req.isThumbnail()))
                .toList();
        return ApiResponse.success(SuccessCode.SUCCESS, response);
    }

    @PostMapping("/complete")
    public ApiResponse<?> completeUpload(@RequestBody List<CompleteRequest> request) {
        currentHouseService.addHouseImages(request);
        return ApiResponse.success(SuccessCode.SUCCESS, "모든 이미지 DB 저장 성공");
    }
}
