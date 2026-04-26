package com.ssafy.backend.domain.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.domain.ai.dto.McpHouseInfo;
import com.ssafy.backend.domain.ai.dto.ScoredMcpHouseInfo;
import com.ssafy.backend.domain.ai.dto.request.RagSearchRequest;
import com.ssafy.backend.domain.ai.dto.request.RerankRequest;
import com.ssafy.backend.domain.ai.dto.response.AiChatResponse;
import com.ssafy.backend.domain.ai.dto.response.RerankResponse;
import com.ssafy.backend.domain.ai.dto.response.ScoredRagSearchResult;
import com.ssafy.backend.domain.ai.entity.AiChatHistory;
import com.ssafy.backend.domain.ai.entity.AiTop3Result;
import com.ssafy.backend.domain.ai.entity.RagSearchResult;
import com.ssafy.backend.domain.ai.repository.AiChatHistoryRepository;
import com.ssafy.backend.domain.code.service.CommonCodeService;
import com.ssafy.backend.domain.house.dto.response.CurrentHouseListResponse;
import com.ssafy.backend.domain.house.entity.CurrentHouse;
import com.ssafy.backend.domain.house.repository.CurrentHouseRepository;
import com.ssafy.backend.domain.region.service.RegionService;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {
    private final RegionService regionService;
    private final CommonCodeService commonCodeService;
    private final ChatClient chatClient;
    private final RerankerClient rerankerClient;
    private final ObjectMapper objectMapper;
    private final AiChatHistoryRepository aiChatHistoryRepository;
    private final CurrentHouseRepository currentHouseRepository;
    private final McpService mcpService;
    private final McpServiceTest mcpServiceTest;

    @Value("classpath:/prompt/filter_create_system.txt")
    private Resource filterCreateSystemPrompt;
    @Value("classpath:/prompt/filter_create_user.txt")
    private Resource filterCreateUserPrompt;

    @Value("classpath:/prompt/top3_recommendation_system.txt")
    private Resource top3RecommendationSystemPrompt;
    @Value("classpath:/prompt/top3_recommendation_user.txt")
    private Resource top3RecommendationUserPrompt;

    public String testChatClient(String userQuery) {
        return chatClient.prompt()
                .system("성공적으로 응답받았으면 다음 문장을 그대로 출력해줘: 나는빡빡이다!")
                .user(spec -> spec.text("안녕하세요"))
                .call()
                .content();
    }

    public List<AiChatHistory> getChatHistory(Long userId) {
        return aiChatHistoryRepository.findTop3ByCreatedByOrderByCreatedAtDesc(userId);
    }

    public void saveChat(String userQuery, String aiAnswer) {
        AiChatHistory chatHistory = new AiChatHistory(userQuery, aiAnswer);
        aiChatHistoryRepository.save(chatHistory);
    }

    public List<ScoredRagSearchResult> rerankSearchResults(String query, List<RagSearchResult> searchResults) {
        if (searchResults.isEmpty()) {
            return Collections.emptyList();
        }

        List<RerankRequest.Candidate> candidates = searchResults.stream()
                .limit(10)
                .map(result -> new RerankRequest.Candidate(
                        String.valueOf(result.houseId()),
                        result.content(),
                        1.0
                ))
                .toList();

        int topK = Math.min(10, searchResults.size());
        RerankRequest request = new RerankRequest(query, candidates, topK);

        try {
            log.info("Rerank Request JSON: {}", objectMapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize RerankRequest", e);
        }

        RerankResponse response = rerankerClient.rerank(request);

        try {
            log.info("Rerank Response JSON: {}", objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize RerankResponse", e);
        }

        Map<Long, RagSearchResult> searchResultMap = searchResults.stream()
                .collect(Collectors.toMap(RagSearchResult::houseId, result -> result));

        return response.results().stream()
                .map(result -> {
                    RagSearchResult original = searchResultMap.get(Long.parseLong(result.id()));
                    return new ScoredRagSearchResult(
                            original.houseId(),
                            original.content(),
                            result.score()
                    );
                })
                .toList();
    }

    public RagSearchRequest parseUserQuery(RagSearchRequest request) {
        var converter = new BeanOutputConverter<RagSearchRequest>(RagSearchRequest.class);

        RagSearchRequest aiResult = chatClient.prompt()
                .system(spec -> spec.text(filterCreateSystemPrompt))
                .user(spec -> spec.text(filterCreateUserPrompt)
                        .params(
                                Map.of("USER_QUERY", request.getQuery()
                                )))
                .call()
                .entity(converter); // 결과를 자동으로 RagSearchRequest 객체로 변환

        request.update(aiResult);

        return request;
    }

    private String posToString(Point pos) {
        return String.format("%f,%f", pos.getY(), pos.getX());
    }

    @Transactional(readOnly = true)
    public AiChatResponse getFinalRecommendation(String userQuery, List<ScoredRagSearchResult> scoredSearchResults, boolean rerankApplied, boolean isTest) {
        var converter = new BeanOutputConverter<>(AiTop3Result.class);

        List<CurrentHouse> houses = currentHouseRepository.findAllById(scoredSearchResults.stream().map(ScoredRagSearchResult::houseId).toList());
        Map<Long, CurrentHouse> houseMap = houses.stream()
                .collect(Collectors.toMap(CurrentHouse::getId, h -> h));


        String searchResultsJson;
        try {
            if (!rerankApplied) {
                List<McpHouseInfo> mcpHouseInfos = scoredSearchResults.stream()
                        .map(ele -> new McpHouseInfo(ele.houseId(), posToString(houseMap.get(ele.houseId()).getPosition()), ele.content())).toList();
                searchResultsJson = objectMapper.writeValueAsString(mcpHouseInfos);
            } else {
                List<ScoredMcpHouseInfo> scoredMcpHouseInfos = scoredSearchResults.stream()
                        .map(ele -> new ScoredMcpHouseInfo(ele.houseId(), ele.score(), posToString(houseMap.get(ele.houseId()).getPosition()), ele.content())).toList();
                searchResultsJson = objectMapper.writeValueAsString(scoredMcpHouseInfos);
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        log.info("@@@@@searchResultsJson: {}", searchResultsJson);
        AiTop3Result aiResults;
        if (isTest) {
            aiResults = chatClient.prompt()
                    .system(top3RecommendationSystemPrompt) // 위에서 정의한 수정된 시스템 프롬프트
                    .user(spec -> spec.text(top3RecommendationUserPrompt) // 위에서 정의한 수정된 사용자 프롬프트
                            .param("USER_QUERY", userQuery)
                            .param("SEARCH_RESULTS", searchResultsJson))
                    .tools(mcpServiceTest)
                    .call()
                    .entity(converter);
        } else {
            aiResults = chatClient.prompt()
                    .system(top3RecommendationSystemPrompt) // 위에서 정의한 수정된 시스템 프롬프트
                    .user(spec -> spec.text(top3RecommendationUserPrompt) // 위에서 정의한 수정된 사용자 프롬프트
                            .param("USER_QUERY", userQuery)
                            .param("SEARCH_RESULTS", searchResultsJson))
                    .tools(mcpService)
                    .call()
                    .entity(converter);
        }
        
        List<Long> recommendationIds = aiResults.recommendations();

        List<CurrentHouse> sortedHouses = recommendationIds.stream()
                .map(houseMap::get)
                .filter(java.util.Objects::nonNull)
                .toList();

        CurrentHouseListResponse houseList = new CurrentHouseListResponse(
                sortedHouses.stream()
                        .map(ele -> CurrentHouseListResponse.ListElement.from(
                                ele,
                                regionService.getSidoName(ele.getSidoCode()),
                                regionService.getSigunguName(ele.getSigunguCode()),
                                regionService.getRegionName(ele.getRegionCode()),
                                commonCodeService.getCommonCodeName(ele.getHouseTypeCode()),
                                commonCodeService.getCommonCodeName(ele.getRentTypeCode()),
                                commonCodeService.getCommonCodeName(ele.getHouseStatusCode()),
                                ele.getFloorCode() == null
                                        ? null
                                        : commonCodeService.getCommonCodeName(ele.getFloorCode())))
                        .toList());

        return new AiChatResponse(aiResults.answer(), houseList.getData());
    }
}
