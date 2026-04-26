package com.ssafy.backend.domain.ai.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class McpServiceTest {
    private static final Logger log = LoggerFactory.getLogger(McpServiceTest.class);

    @Tool(description = "제공된 매물 리스트(ID, 좌표)를 기반으로 특정 목적지까지의 출퇴근 소요 시간과 비용을 분석합니다.")
    public String findCommuteTimeAndCostTest(
            @ToolParam(description = "매물 정보 객체 리스트. 각 객체는 'houseId'(매물번호)와 'coordinates'(위경도 문자열, 예: '37.123,127.456') 필드를 반드시 포함해야 합니다.") List<ItemLocation> items,
            @ToolParam(description = "사용자가 직장이나 학교로 설정한 목적지의 도로명 주소 (예: '서울특별시 강남구 테헤란로 123')") String destination) {
        log.info("--- [출퇴근 분석 시작] 목적지: {}, 매물 개수: {}개 ---", destination, items.size());

        if (destination == null || destination.length() < 5) {
            log.warn("검증 실패: 유효하지 않은 목적지 주소 수신");
            return "ERROR: 정확한 목적지 주소를 입력해주세요.";
        }

        try {
            String analysisResult = items.stream()
                    .map(item -> {
                        int randomMinutes = ThreadLocalRandom.current().nextInt(10, 61);
                        int randomCost = ThreadLocalRandom.current().nextInt(10, 51) * 100;

                        // 각 매물별 분석 로그 기록
                        log.debug("매물 분석 중 - ID: {}, 좌표: {}, 결과: {}분/{}원",
                                item.houseId(), item.coordinates(), randomMinutes, randomCost);

                        return String.format("- [houseId: %s] 소요 시간: %d분,   예상 비용: %,d원",
                                item.houseId(), randomMinutes, randomCost);
                    })
                    .collect(Collectors.joining("\n"));

            log.info("--- [출퇴근 분석 완료] ---");

            return String.format("### 목적지 [%s] 기준 출퇴근 분석 결과\n%s", destination, analysisResult);
        } catch (Exception e) {
            log.error("출퇴근 분석 중 오류 발생", e);
            return "ERROR: 출퇴근 소요 시간 및 비용 분석 중 오류가 발생했습니다.";
        }
    }

    @Tool(description = "특정 매물 ID 리스트를 입력받아 각 매물 반경 내 모든 인프라(BUS, SUBWAY, CONVENIENCE, LAUNDRY, CAFE, HOSPITAL, PHARMACY)의 개수와 최단 거리를 반환합니다.")
    public String findNearbyInfrastructureTest(
            @ToolParam(description = "조회할 매물 ID 리스트") List<Long> houseIds) {

        log.info("--- [주변 인프라 조회] 매물 ID 리스트: {} ---", houseIds);

        if (houseIds == null || houseIds.isEmpty()) {
            return "ERROR: 조회할 매물 ID 리스트가 비어있습니다.";
        }

        try {
            List<String> types = Arrays.asList("BUS", "SUBWAY", "CONVENIENCE", "LAUNDRY", "CAFE", "HOSPITAL", "PHARMACY");

            StringBuilder allResults = new StringBuilder();

            for (Long houseId : houseIds) {
                String result = types.stream()
                        .map(type -> {
                            int randomCount = ThreadLocalRandom.current().nextInt(0, 100);
                            int randomDistance = ThreadLocalRandom.current().nextInt(50, 800);
                            String distanceStr = randomCount > 0 ? randomDistance + "m" : "정보 없음";

                            return String.format("  - %s: 개수 %d개, 최단 거리 %s",
                                    type,
                                    randomCount,
                                    distanceStr);
                        })
                        .collect(Collectors.joining("\n"));

                String houseResult = String.format("매물 ID [%d]의 주변 인프라 정보:\n%s\n\n", houseId, result);
                allResults.append(houseResult);

                log.info("인프라 분석 결과 - {}", houseResult.trim());
            }

            return allResults.toString().trim();

        } catch (Exception e) {
            log.error("주변 인프라 조회 중 오류 발생", e);
            return "ERROR: 인프라 정보 조회 중 오류가 발생했습니다.";
        }
    }

    public record ItemLocation(String houseId, String coordinates) {
    }

}
