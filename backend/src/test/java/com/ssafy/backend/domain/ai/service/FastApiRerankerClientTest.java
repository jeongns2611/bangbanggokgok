package com.ssafy.backend.domain.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.domain.ai.dto.request.RerankRequest;
import com.ssafy.backend.domain.ai.dto.response.RerankResponse;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class FastApiRerankerClientTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void rerank_returnsSortedResultsFromFastApi() throws Exception {
        AtomicReference<String> requestBodyRef = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/v1/rerank", exchange -> {
            requestBodyRef.set(readBody(exchange));
            writeJson(exchange, 200, """
                    {
                      "results": [
                        { "id": "house-2", "score": 0.9451, "rank": 1 },
                        { "id": "house-1", "score": 0.8132, "rank": 2 }
                      ]
                    }
                    """);
        });
        server.start();

        FastApiRerankerClient client = new FastApiRerankerClient(baseUrl());
        RerankRequest request = new RerankRequest(
                "강남역까지 출퇴근 편한 원룸",
                List.of(
                        new RerankRequest.Candidate("house-1", "2호선 도보 5분 원룸", 0.72),
                        new RerankRequest.Candidate("house-2", "강남역 버스 10분 투룸", 0.69)
                ),
                2
        );

        RerankResponse response = client.rerank(request);

        assertThat(response.results()).hasSize(2);
        assertThat(response.results().get(0).id()).isEqualTo("house-2");
        assertThat(response.results().get(0).score()).isEqualTo(0.9451);
        assertThat(response.results().get(0).rank()).isEqualTo(1);

        JsonNode root = OBJECT_MAPPER.readTree(requestBodyRef.get());
        assertThat(root.path("query").asText()).isEqualTo("강남역까지 출퇴근 편한 원룸");
        assertThat(root.path("topK").asInt()).isEqualTo(2);
        assertThat(root.path("candidates")).hasSize(2);
        assertThat(root.path("candidates").get(0).path("id").asText()).isEqualTo("house-1");
        assertThat(root.path("candidates").get(0).path("originalScore").asDouble()).isEqualTo(0.72);
    }

    @Test
    void rerank_rejectsBlankQuery() {
        FastApiRerankerClient client = new FastApiRerankerClient("http://localhost:8000");

        assertThatThrownBy(() -> client.rerank(new RerankRequest(" ", List.of(
                new RerankRequest.Candidate("house-1", "문서", 0.5)
        ), 1)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    private String baseUrl() {
        return "http://localhost:" + server.getAddress().getPort();
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void writeJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, responseBytes.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(responseBytes);
        }
    }
}
