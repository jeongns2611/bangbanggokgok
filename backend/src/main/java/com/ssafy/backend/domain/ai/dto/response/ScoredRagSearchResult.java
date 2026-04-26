package com.ssafy.backend.domain.ai.dto.response;

public record ScoredRagSearchResult(
        Long houseId,
        String content,
        Double score
) {
}
