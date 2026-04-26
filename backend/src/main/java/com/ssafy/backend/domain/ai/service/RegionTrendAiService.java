package com.ssafy.backend.domain.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.domain.ai.dto.response.RegionTrendAiResponse;
import com.ssafy.backend.domain.region.entity.RegionSigungu;
import com.ssafy.backend.domain.region.repository.RegionSigunguRepository;
import com.ssafy.backend.domain.statistics.dto.response.RegionMonthlyStatisticResponse;
import com.ssafy.backend.domain.statistics.dto.response.StatisticTrendDto;
import com.ssafy.backend.domain.statistics.service.StatisticsService;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionTrendAiService {

    private static final String TITLE = "AI Insight";
    private static final String SUMMARY_TITLE = "AI 요약";
    private static final String DEFAULT_DISCLAIMER =
            "최근 1년 거래 데이터를 바탕으로 계절성과 가격 흐름을 요약한 참고 분석입니다.";

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final RegionSigunguRepository regionSigunguRepository;
    private final StatisticsService statisticsService;

    @Value("classpath:/prompt/region_trend_system.txt")
    private Resource regionTrendSystemPrompt;

    @Value("classpath:/prompt/region_trend_user.txt")
    private Resource regionTrendUserPrompt;

    public RegionTrendAiResponse summarizeMonthlyTrend(String sigunguName) {
        log.info("[RegionTrendAiService.summarizeMonthlyTrend] 지역 거래 동향 AI 요청 시작. sigunguName={}", sigunguName);

        RegionSigungu regionSigungu = resolveSigungu(sigunguName);
        String sidoName = regionSigungu.getRegionSido().getSidoName();
        String normalizedSigunguName = regionSigungu.getSigunguName();
        String sigunguCode = regionSigungu.getSigunguCode();

        RegionMonthlyStatisticResponse response =
                statisticsService.getMonthlyRegionStatistics(sidoName, normalizedSigunguName);
        List<StatisticTrendDto> trends = response.getTrends();

        if (trends.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "해당 지역구의 월별 거래 통계가 없습니다.");
        }

        try {
            log.info(
                    "[RegionTrendAiService.summarizeMonthlyTrend] 지역 거래 동향 AI 호출 시작. sigunguCode={}, sigunguName={}",
                    sigunguCode,
                    normalizedSigunguName
            );
            String content = chatClient.prompt()
                    .system(sp -> sp.text(regionTrendSystemPrompt))
                    .user(up -> up.text(regionTrendUserPrompt)
                            .param("SIDO_NAME", sidoName)
                            .param("SIGUNGU_NAME", normalizedSigunguName)
                            .param("TREND_LINES", buildTrendLines(trends)))
                    .call()
                    .content();

            if (content == null || content.isBlank()) {
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "지역 거래 동향 AI 응답이 비어 있습니다.");
            }

            RegionTrendInsightPayload payload =
                    objectMapper.readValue(stripCodeFence(content), RegionTrendInsightPayload.class);

            List<String> insights = payload.insights() == null ? List.of() : payload.insights().stream()
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .toList();

            if (insights.isEmpty()) {
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "지역 거래 동향 AI 인사이트가 비어 있습니다.");
            }

            String disclaimer = payload.disclaimer() == null ? "" : payload.disclaimer().trim();
            if (disclaimer.isBlank()) {
                disclaimer = DEFAULT_DISCLAIMER;
            }

            log.info(
                    "[RegionTrendAiService.summarizeMonthlyTrend] 지역 거래 동향 AI 호출 완료. sigunguCode={}, sigunguName={}",
                    sigunguCode,
                    normalizedSigunguName
            );

            return new RegionTrendAiResponse(
                    TITLE,
                    SUMMARY_TITLE,
                    sidoName,
                    normalizedSigunguName,
                    sigunguCode,
                    trends.get(0).getBaseYearMonth(),
                    insights,
                    disclaimer
            );
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "지역 거래 동향 AI 응답 파싱에 실패했습니다.");
        } catch (RuntimeException e) {
            if (e instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "지역 거래 동향 AI 응답 생성에 실패했습니다.");
        }
    }

    private RegionSigungu resolveSigungu(String sigunguName) {
        String normalizedSigunguName = sigunguName == null ? "" : sigunguName.trim();
        if (normalizedSigunguName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "sigunguName은 필수입니다.");
        }

        List<RegionSigungu> candidates = regionSigunguRepository.findAllBySigunguName(normalizedSigunguName);
        if (candidates.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "해당 시군구를 찾을 수 없습니다.");
        }
        if (candidates.size() > 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "동일한 sigunguName이 여러 시도에 존재합니다.");
        }
        return candidates.get(0);
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

    private String buildTrendLines(List<StatisticTrendDto> trends) {
        StringBuilder builder = new StringBuilder();
        for (StatisticTrendDto trend : trends) {
            builder.append("- ")
                    .append(formatYearMonth(trend.getBaseYearMonth()))
                    .append(": 거래 ")
                    .append(trend.getTransactionCount())
                    .append("건, 평균 전세 ")
                    .append(trend.getAvgJeonSae())
                    .append(", 평균 월세 보증금 ")
                    .append(trend.getAvgRentDeposit())
                    .append(", 평균 월세 ")
                    .append(trend.getAvgRent())
                    .append('\n');
        }
        return builder.toString();
    }

    private String formatYearMonth(Integer baseYearMonth) {
        String value = String.valueOf(baseYearMonth);
        if (value.length() != 6) {
            return value;
        }
        return value.substring(0, 4) + "-" + value.substring(4);
    }

    private record RegionTrendInsightPayload(
            List<String> insights,
            String disclaimer
    ) {
    }
}
