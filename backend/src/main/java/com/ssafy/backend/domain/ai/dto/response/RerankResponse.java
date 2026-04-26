package com.ssafy.backend.domain.ai.dto.response;

import java.util.List;

public record RerankResponse(
        List<Result> results
) {

    public record Result(
            String id,
            Double score,
            Integer rank
    ) {
    }
}
