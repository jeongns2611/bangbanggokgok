package com.ssafy.backend.domain.ai.dto.response;

import java.util.List;

public record RegionTrendAiResponse(
        String title,
        String summaryTitle,
        String sidoName,
        String sigunguName,
        String sigunguCode,
        Integer latestBaseYearMonth,
        List<String> insights,
        String disclaimer
) {
}
