package com.ssafy.backend.domain.ai.service;

import com.ssafy.backend.domain.ai.dto.request.RerankRequest;
import com.ssafy.backend.domain.ai.dto.response.RerankResponse;

public interface RerankerClient {

    RerankResponse rerank(RerankRequest request);
}
