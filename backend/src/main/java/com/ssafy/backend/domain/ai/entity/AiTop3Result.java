package com.ssafy.backend.domain.ai.entity;

import java.util.List;

public record AiTop3Result(
        String answer,
        List<Long> recommendations
) {
}
