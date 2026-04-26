package com.ssafy.backend.domain.ai.service;

import com.ssafy.backend.domain.commute.service.CommuteMetricsService;
import com.ssafy.backend.domain.commute.service.CommuteMetricsService.McpCommuteMetrics;
import com.ssafy.backend.domain.infrastructure.dto.response.NearbyInfrastructureResponse;
import com.ssafy.backend.domain.infrastructure.service.InfrastructureService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class McpService {
    private static final Logger log = LoggerFactory.getLogger(McpService.class);

    private final InfrastructureService infrastructureService;
    private final CommuteMetricsService commuteMetricsService;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Tool(description = "제공된 매물 리스트(ID, 좌표)를 기반으로 특정 목적지 통근 시간, 이동 거리, 환승 횟수 및 편도 비용 정보를 분석합니다.")
    public String findCommuteTimeAndCost(
            @ToolParam(description = "매물 정보 객체 리스트. 각 객체는 'houseId'(매물번호)와 'coordinates'(위경도 문자열, 예: '37.123, 127.456') 필드를 반드시 포함해야 합니다.") List<ItemLocation> items,
            @ToolParam(description = "사용자가 설정한 목적지의 위경도 좌표 문자열 (예: '37.123, 127.456')") String destinationCoordinates) {
        log.info("--- [출퇴근 분석 시작] 목적지 좌표: {}, 매물 개수: {}개 ---", destinationCoordinates, items.size());

        if (destinationCoordinates == null || !destinationCoordinates.contains(",")) {
            log.warn("검증 실패: 유효하지 않은 목적지 좌표 수신");
            return "ERROR: 정확한 목적지 위경도 좌표를 입력해주세요.";
        }

        try {
            Point targetPos = createPointFromCoordinates(destinationCoordinates);

            String analysisResult = items.stream()
                    .map(item -> {
                        try {
                            Point housePos = createPointFromCoordinates(item.coordinates());
                            McpCommuteMetrics metrics = commuteMetricsService.getOrCreateForMCP(housePos, targetPos);

                            Integer minutes = metrics.totalTime();
                            Double distance = metrics.totalDistance();
                            Integer transitCount = metrics.transitCount();
                            Integer cost = metrics.cost();

                            log.debug("매물 분석 완료 - ID: {}, 소요시간: {}분, 거리: {}km, 환승: {}회, 비용: {}원",
                                    item.houseId(), minutes, distance, transitCount, cost);

                            return String.format("- [houseId: %s] 소요 시간: %d분, 이동 거리: %.1fkm, 환승 횟수: %d회, 편도 비용: %,d원",
                                    item.houseId(),
                                    minutes != null ? minutes : 0,
                                    distance != null ? distance : 0.0,
                                    transitCount != null ? transitCount : 0,
                                    cost != null ? cost : 0);
                        } catch (Exception ex) {
                            return String.format("- [houseId: %s] 통근 분석 오류: %s", item.houseId(), ex.getMessage());
                        }
                    })
                    .collect(Collectors.joining("\n"));

            log.info("--- [출퇴근 분석 완료] ---");

            return String.format("### 목적지 좌표 [%s] 기준 출퇴근 분석 결과\n%s", destinationCoordinates, analysisResult);
        } catch (Exception e) {
            log.error("출퇴근 분석 중 오류 발생", e);
            return "ERROR: 출퇴근 소요 시간 및 거리 분석 중 오류가 발생했습니다.";
        }
    }

    private Point createPointFromCoordinates(String coordinatesStr) {
        String[] parts = coordinatesStr.split(",");
        double lat = Double.parseDouble(parts[0].trim());
        double lng = Double.parseDouble(parts[1].trim());
        return geometryFactory.createPoint(new Coordinate(lng, lat));
    }

    @Tool(description = "특정 매물 ID 리스트를 입력받아 각 매물 반경 내 모든 인프라(BUS, SUBWAY, CONVENIENCE, LAUNDRY, CAFE, HOSPITAL, PHARMACY)의 개수와 최단 거리를 반환합니다.")
    public String findNearbyInfrastructure(
            @ToolParam(description = "조회할 매물 ID 리스트") List<Long> houseIds) {

        log.info("--- [주변 인프라 조회] 매물 ID 리스트: {} ---", houseIds);

        if (houseIds == null || houseIds.isEmpty()) {
            return "ERROR: 조회할 매물 ID 리스트가 비어있습니다.";
        }

        try {
            StringBuilder allResults = new StringBuilder();

            for (Long houseId : houseIds) {
                NearbyInfrastructureResponse nearby = infrastructureService.getNearbyInfrastructures(houseId);

                String result = nearby.getSummaries().stream()
                        .map(summary -> {
                            String distanceStr = summary.getCount() > 0 && summary.getNearestDistanceMeters() != null
                                    ? summary.getNearestDistanceMeters() + "m"
                                    : "정보 없음";

                            return String.format("  - %s: 개수 %d개, 최단 거리 %s",
                                    summary.getType(),
                                    summary.getCount(),
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
