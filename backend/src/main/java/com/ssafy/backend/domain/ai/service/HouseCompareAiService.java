package com.ssafy.backend.domain.ai.service;

import com.ssafy.backend.domain.ai.dto.response.HouseCompareAiResponse;
import com.ssafy.backend.domain.house.dto.request.HouseCompareAiRequest;
import com.ssafy.backend.global.cacheable.CacheStrategy;
import com.ssafy.backend.global.cacheable.MyCacheable;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HouseCompareAiService {

    private final HouseCompareAiChatService houseCompareAiChatService;

    public static String normalizeCacheKey(HouseCompareAiRequest compareRequest, Long userId) {
        if (compareRequest == null || compareRequest.comparisonData() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "비교 데이터가 필요합니다.");
        }

        String normalizedIds = compareRequest.comparisonData().stream()
                .map(HouseCompareAiRequest.HouseItem::houseId)
                .sorted(Comparator.naturalOrder())
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        if (normalizedIds.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "비교 대상 매물 정보가 필요합니다.");
        }

        return userId + ":" + normalizedIds;
    }

    @MyCacheable(
            cacheName = "houseCompareAi",
            key = "T(com.ssafy.backend.domain.ai.service.HouseCompareAiService).normalizeCacheKey(#compareRequest, #userId)",
            ttlSeconds = 1800,
            cacheStrategy = CacheStrategy.LOOK_ASIDE
    )
    public HouseCompareAiResponse compare(HouseCompareAiRequest compareRequest, Long userId) {
        validateCompareRequest(compareRequest);
        return houseCompareAiChatService.generate(compareRequest);
    }

    private void validateCompareRequest(HouseCompareAiRequest compareRequest) {
        if (compareRequest == null || compareRequest.comparisonData() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "비교 데이터가 필요합니다.");
        }

        List<HouseCompareAiRequest.HouseItem> comparisonData = compareRequest.comparisonData();
        if (comparisonData.size() != 2) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "AI 비교는 정확히 두 개의 매물만 비교할 수 있습니다.");
        }

        boolean hasMissingHouseId = comparisonData.stream()
                .map(HouseCompareAiRequest.HouseItem::houseId)
                .anyMatch(houseId -> houseId == null);
        if (hasMissingHouseId) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "비교 대상 매물 id가 필요합니다.");
        }
    }
}
