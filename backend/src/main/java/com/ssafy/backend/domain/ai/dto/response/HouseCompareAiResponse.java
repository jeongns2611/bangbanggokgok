package com.ssafy.backend.domain.ai.dto.response;

import java.util.List;

public record HouseCompareAiResponse(
        String overallEvaluation,
        Long recommendedHouseId,
        List<HouseEvaluation> houses
) {

    public record HouseEvaluation(
            Long houseId,
            String houseLabel,
            List<String> recommendationReasons,
            List<String> cautionReasons
    ) {
    }
}
