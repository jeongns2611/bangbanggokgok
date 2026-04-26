package com.ssafy.backend.global.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;


@Configuration
public class AiConfig {
    @Value("${spring.ai.openai.base-url}")
    private String openAiBaseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Bean
    EmbeddingModel embeddingModel() {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(openAiBaseUrl)
                .apiKey(openAiApiKey)
                .build();

        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder()
                        .model("text-embedding-3-large")
                        .build(),
                RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    @Bean
    ChatClient chatClient() {
        // 0. 타임아웃 설정 (120초)
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setReadTimeout(60 * 1000); // 읽기 시간 초과 60초
        requestFactory.setConnectTimeout(30 * 1000); // 연결 시간 초과 30초

        // 1. 하위 API 설정 (Base URL, API Key, Custom Client)
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(openAiBaseUrl)
                .apiKey(openAiApiKey)
                .restClientBuilder(RestClient.builder().requestFactory(requestFactory)) // 타임아웃 적용된 Client 주입
                .build();


        // 2. ChatModel 설정 (모델명, 온도 등)
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model("gpt-5.2")
                                .temperature(0.7d)
                                .build()
                )
                .build();

        // 3. 최종 ChatClient 생성 및 반환
        return ChatClient.builder(chatModel).build();
    }
}