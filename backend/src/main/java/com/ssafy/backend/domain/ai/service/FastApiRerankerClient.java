package com.ssafy.backend.domain.ai.service;

import com.ssafy.backend.domain.ai.dto.request.RerankRequest;
import com.ssafy.backend.domain.ai.dto.response.RerankResponse;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Service
public class FastApiRerankerClient implements RerankerClient {

    private final WebClient webClient;
    private final Duration timeout;
    private final Retry retrySpec;

    public FastApiRerankerClient(String baseUrl) {
        this(baseUrl, 30000L, 2L, 200L);
    }

    @Autowired
    public FastApiRerankerClient(
            @Value("${reranker.base-url}") String baseUrl,
            @Value("${reranker.timeout-ms}") long timeoutMs,
            @Value("${reranker.retry-count}") long retryCount,
            @Value("${reranker.retry-delay-ms}") long retryDelayMs
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.timeout = Duration.ofMillis(timeoutMs);
        this.retrySpec = Retry.fixedDelay(retryCount, Duration.ofMillis(retryDelayMs))
                .filter(this::isRetryable)
                .doBeforeRetry(signal -> log.warn(
                        "FastAPI reranker 재시도: attempt={}, reason={}",
                        signal.totalRetries() + 1,
                        signal.failure().getClass().getSimpleName()
                ));
    }

    @Override
    public RerankResponse rerank(RerankRequest request) {
        validateRequest(request);
        int candidateCount = request.candidates().size();
        int topK = request.topK() == null ? candidateCount : request.topK();
        log.info("Reranker request start candidateCount={} topK={} queryLength={}",
                candidateCount,
                topK,
                request.query().length());

        try {
            RerankResponse response = webClient.post()
                    .uri("/api/v1/rerank")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .map(body -> new BusinessException(
                                            ErrorCode.EXTERNAL_API_ERROR,
                                            "FastAPI reranker 호출 실패: " + clientResponse.statusCode()
                                    )))
                    .bodyToMono(RerankResponse.class)
                    .timeout(timeout)
                    .retryWhen(retrySpec)
                    .block();

            if (response == null || response.results() == null) {
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "FastAPI reranker 응답이 비어 있습니다.");
            }

            log.info("Reranker request completed candidateCount={} resultCount={}",
                    candidateCount,
                    response.results().size());
            return response;
        } catch (WebClientResponseException e) {
            log.warn("FastAPI reranker 응답 오류: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "FastAPI reranker 호출 실패: " + e.getStatusCode());
        } catch (WebClientRequestException e) {
            log.warn("FastAPI reranker 요청 오류", e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "FastAPI reranker 연결 실패");
        } catch (RuntimeException e) {
            Throwable rootCause = Exceptions.unwrap(e);
            if (rootCause instanceof BusinessException businessException) {
                throw businessException;
            }
            if (rootCause instanceof java.util.concurrent.TimeoutException) {
                log.warn("FastAPI reranker 요청 타임아웃: {}ms", timeout.toMillis());
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "FastAPI reranker 요청 타임아웃");
            }
            log.warn("FastAPI reranker 호출 중 예외", rootCause);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "FastAPI reranker 호출 실패");
        }
    }

    private boolean isRetryable(Throwable throwable) {
        Throwable rootCause = Exceptions.unwrap(throwable);
        return rootCause instanceof WebClientRequestException
                || rootCause instanceof java.util.concurrent.TimeoutException;
    }

    private void validateRequest(RerankRequest request) {
        if (request == null || request.query() == null || request.query().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "rerank query는 비어 있을 수 없습니다.");
        }
        if (request.candidates() == null || request.candidates().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "rerank 후보 목록은 1개 이상이어야 합니다.");
        }
    }
}
