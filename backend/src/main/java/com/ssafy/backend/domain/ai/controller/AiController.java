package com.ssafy.backend.domain.ai.controller;

import com.ssafy.backend.batch.service.BatchService;
import com.ssafy.backend.batch.service.RagService;
import com.ssafy.backend.domain.ai.dto.request.AiChatRequest;
import com.ssafy.backend.domain.ai.dto.request.RagSearchRequest;
import com.ssafy.backend.domain.ai.dto.response.AiChatResponse;
import com.ssafy.backend.domain.ai.dto.response.RegionTrendAiResponse;
import com.ssafy.backend.domain.ai.dto.response.ScoredRagSearchResult;
import com.ssafy.backend.domain.ai.entity.AiChatHistory;
import com.ssafy.backend.domain.ai.entity.RagSearchResult;
import com.ssafy.backend.domain.ai.service.AiService;
import com.ssafy.backend.domain.ai.service.RegionTrendAiService;
import com.ssafy.backend.global.auth.principal.CustomOAuth2User;
import com.ssafy.backend.global.common.response.ApiResponse;
import com.ssafy.backend.global.error.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiService aiService;
    private final BatchService batchService;
    private final RagService ragService;
    private final RegionTrendAiService regionTrendAiService;

    @GetMapping
    public ApiResponse<?> aiChatTest() {
        String response = aiService.testChatClient("");
        return ApiResponse.success(SuccessCode.SUCCESS, response);
    }

    @GetMapping("/batch")
    public ApiResponse<?> batchOutbox() {
        batchService.processDailyOutbox();
        return ApiResponse.success(SuccessCode.SUCCESS);
    }

    @GetMapping("/chat")
    public ApiResponse<?> getChatHistory(@AuthenticationPrincipal CustomOAuth2User userDetails) {
        Long userId = userDetails != null ? userDetails.getUserId() : 0L;
        List<AiChatHistory> chatHistory = aiService.getChatHistory(userId);
        Collections.reverse(chatHistory);
        return ApiResponse.success(SuccessCode.SUCCESS, chatHistory);
    }

    @PostMapping("/chat")
    public ApiResponse<?> chatWithAi(@RequestParam(name = "reranked", defaultValue = "false") Boolean reranked, @RequestParam(name = "test", defaultValue = "true") Boolean test, @RequestBody AiChatRequest request) {
        if (request.getQuery().equals("batch")) {
            batchService.processDailyOutbox();
            return ApiResponse.success(SuccessCode.SUCCESS, "배치작업완료");
        }
        RagSearchRequest rawRequest = new RagSearchRequest(request);
        RagSearchRequest searchRequest = aiService.parseUserQuery(rawRequest);
        List<RagSearchResult> searchResults = ragService.search(request.getQuery(), searchRequest);

        List<ScoredRagSearchResult> finalResults;
        if (Boolean.TRUE.equals(reranked)) {
            finalResults = aiService.rerankSearchResults(request.getQuery(), searchResults);
            log.info("--- After Reranking ---");
            finalResults.forEach(res -> log.info("HouseId: {}, Score: {}, Content: {}", res.houseId(), res.score(), res.content()));
        } else {
            finalResults = searchResults.stream()
                    .map(res -> new ScoredRagSearchResult(res.houseId(), res.content(), 0.0))
                    .toList();
        }

        AiChatResponse aiChatResponse = aiService.getFinalRecommendation(request.getQuery(), finalResults, reranked, test);
        aiService.saveChat(request.getQuery(), aiChatResponse.getAnswer());
        return ApiResponse.success(SuccessCode.SUCCESS, aiChatResponse);
    }

    @PostMapping("/reranker/test")
    public ApiResponse<?> rerankerTest(@RequestBody AiChatRequest request) {
        if (request.getQuery().equals("batch")) {
            batchService.processDailyOutbox();
            return ApiResponse.success(SuccessCode.SUCCESS, "배치작업완료");
        }
        RagSearchRequest rawRequest = new RagSearchRequest(request);
        RagSearchRequest searchRequest = aiService.parseUserQuery(rawRequest);
        List<RagSearchResult> searchResults = ragService.searchRerankTest(request.getQuery(), searchRequest);
        List<ScoredRagSearchResult> rerankedResults = aiService.rerankSearchResults(request.getQuery(), searchResults);
        log.info("--- After Reranking (rerankedResults) ---");
        rerankedResults.forEach(res -> log.info("HouseId: {}, Score: {}, Content: {}", res.houseId(), res.score(), res.content()));

        return ApiResponse.success(SuccessCode.SUCCESS);
    }

    @PostMapping("/rag")
    public ApiResponse<?> ragSearch(@RequestBody RagSearchRequest request) {
        List<RagSearchResult> response = ragService.search(request.getQuery(), request);

        return ApiResponse.success(SuccessCode.SUCCESS, response);
    }

    @PostMapping("/filter")
    public ApiResponse<?> getFilter(@RequestBody AiChatRequest request) {
        RagSearchRequest rawRequest = new RagSearchRequest(request);
        RagSearchRequest response = aiService.parseUserQuery(rawRequest);

        return ApiResponse.success(SuccessCode.SUCCESS, response);
    }

    @GetMapping("/regions/monthly-trends")
    public ApiResponse<RegionTrendAiResponse> summarizeRegionMonthlyTrend(
            @RequestParam("sigunguName") String sigunguName
    ) {
        return ApiResponse.success(
                SuccessCode.SUCCESS,
                regionTrendAiService.summarizeMonthlyTrend(sigunguName)
        );
    }
}

//    private String houseType; // 오피스텔, 연립/다세대(빌라), 단독/다가구(원룸), 아파트
//    private String rentType; // 전세, 월세
//    private String floor; // 지상층, 반지하, 1층
//    private String address; // 도로명주소
//    private Integer deposit; // 보증금
//    private Integer monthlyCost; // 월세
//    private Integer managementCost; // 관리비
//    private String managementItems; // 관리비항목(전기 등)
//    private Double floorSize; // 전용면적
//    private Integer buildYear; // 건축년도