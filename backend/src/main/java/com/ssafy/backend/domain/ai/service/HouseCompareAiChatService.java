package com.ssafy.backend.domain.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.domain.ai.dto.response.HouseCompareAiResponse;
import com.ssafy.backend.domain.house.dto.request.HouseCompareAiRequest;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HouseCompareAiChatService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Value("classpath:/prompt/house_compare_system.txt")
    private Resource houseCompareSystemPrompt;
    @Value("classpath:/prompt/house_compare_user.txt")
    private Resource houseCompareUserPrompt;

    public HouseCompareAiResponse generate(HouseCompareAiRequest compareRequest) {
        String content = null;
        try {
            String compareJson = objectMapper.writeValueAsString(compareRequest);
            content = chatClient.prompt()
                    .system(sp -> sp.text(houseCompareSystemPrompt))
                    .user(up -> up.text(houseCompareUserPrompt)
                            .param("COMPARE_JSON", compareJson))
                    .call()
                    .content();

            if (content == null || content.isBlank()) {
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "AI 응답이 비어 있습니다.");
            }

            return objectMapper.readValue(stripCodeFence(content), HouseCompareAiResponse.class);
        } catch (JsonProcessingException e) {
            log.error("[HouseCompareAiChatService.generate] failed to parse AI response. content={}", content, e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "AI 응답을 파싱하는 중 오류가 발생했습니다.");
        } catch (RuntimeException e) {
            if (e instanceof BusinessException businessException) {
                throw businessException;
            }
            log.error("[HouseCompareAiChatService.generate] failed to create AI comparison response", e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "AI 비교 응답 생성에 실패했습니다.");
        }
    }

    private String stripCodeFence(String content) {
        String trimmed = content.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }

        int firstLineBreak = trimmed.indexOf('\n');
        int lastFence = trimmed.lastIndexOf("```");
        if (firstLineBreak < 0 || lastFence <= firstLineBreak) {
            return trimmed;
        }

        return trimmed.substring(firstLineBreak + 1, lastFence).trim();
    }
}
