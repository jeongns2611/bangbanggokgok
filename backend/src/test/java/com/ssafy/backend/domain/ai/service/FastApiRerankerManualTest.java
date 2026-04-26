package com.ssafy.backend.domain.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.backend.domain.ai.dto.request.RerankRequest;
import com.ssafy.backend.domain.ai.dto.response.RerankResponse;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class FastApiRerankerManualTest {

    @Test
    void manualSmokeTest_printsResponseAndLatency() throws Exception {
        String enabled = System.getProperty("reranker.manual.enabled", "false");
        Assumptions.assumeTrue(Boolean.parseBoolean(enabled),
                "Pass -Dreranker.manual.enabled=true to run this test.");

        String baseUrl = System.getenv().getOrDefault("RERANKER_BASE_URL", "http://localhost:8000");
        Assumptions.assumeTrue(isReachable(baseUrl + "/api/v1/health"),
                () -> "Reranker is not reachable: " + baseUrl);

        FastApiRerankerClient client = new FastApiRerankerClient(baseUrl);
        RerankRequest request = new RerankRequest(
                "강남역까지 30분 이내 출퇴근 가능하고 편의점이 가깝고 월세 80 이하인 원룸",
                List.of(
                        new RerankRequest.Candidate("house-101", "역삼역 도보 8분, 강남역 지하철 14분, 편의점 1분, 월세 75, 원룸 22m2", 0.81),
                        new RerankRequest.Candidate("house-102", "선릉역 도보 5분, 강남역 지하철 18분, 편의점 3분, 월세 78, 투룸 31m2", 0.76),
                        new RerankRequest.Candidate("house-103", "강남역 버스 18분, 편의점 30초, 월세 72, 오피스텔 원룸 24m2", 0.79),
                        new RerankRequest.Candidate("house-104", "신논현역 도보 10분, 강남역 도보 15분, 편의점 2분, 월세 83, 원룸 20m2", 0.84),
                        new RerankRequest.Candidate("house-105", "양재역 도보 6분, 강남역 지하철 9분, 편의점 4분, 월세 68, 원룸 19m2", 0.74),
                        new RerankRequest.Candidate("house-106", "사당역 도보 4분, 강남역 지하철 24분, 편의점 1분, 월세 62, 원룸 18m2", 0.71),
                        new RerankRequest.Candidate("house-107", "건대입구역 도보 7분, 강남역 지하철 33분, 편의점 2분, 월세 70, 원룸 23m2", 0.73),
                        new RerankRequest.Candidate("house-108", "강남역 도보 11분, 편의점 5분, 월세 88, 원룸 21m2", 0.86),
                        new RerankRequest.Candidate("house-109", "교대역 도보 9분, 강남역 지하철 6분, 편의점 1분, 월세 79, 원룸 20m2", 0.82),
                        new RerankRequest.Candidate("house-110", "서울대입구역 도보 3분, 강남역 버스 28분, 편의점 30초, 월세 65, 원룸 17m2", 0.69)
                ),
                10
        );

        int iterations = Integer.getInteger("reranker.manual.iterations", 5);
        long totalNanos = 0L;
        RerankResponse lastResponse = null;

        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            lastResponse = client.rerank(request);
            totalNanos += (System.nanoTime() - start);
        }

        long averageMillis = Duration.ofNanos(totalNanos / iterations).toMillis();
        System.out.println("RERANKER_BASE_URL=" + baseUrl);
        System.out.println("iterations=" + iterations);
        System.out.println("avgLatencyMs=" + averageMillis);
        System.out.println("response=" + lastResponse);

        assertThat(lastResponse).isNotNull();
        assertThat(lastResponse.results()).isNotEmpty();
        assertThat(lastResponse.results()).allSatisfy(result -> {
            assertThat(result.id()).isNotBlank();
            assertThat(result.rank()).isPositive();
            assertThat(result.score()).isNotNull();
        });
    }

    private boolean isReachable(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setConnectTimeout(1500);
            connection.setReadTimeout(1500);
            connection.setRequestMethod("GET");
            int statusCode = connection.getResponseCode();
            return statusCode >= 200 && statusCode < 500;
        } catch (Exception e) {
            return false;
        }
    }
}
