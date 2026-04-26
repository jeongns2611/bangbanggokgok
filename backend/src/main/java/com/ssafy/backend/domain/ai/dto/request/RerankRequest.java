package com.ssafy.backend.domain.ai.dto.request;

import java.util.List;

public record RerankRequest(
        String query,
        List<Candidate> candidates,
        Integer topK
) {

    public record Candidate(
            String id,
            String text,
            Double originalScore
    ) {
    }
}
