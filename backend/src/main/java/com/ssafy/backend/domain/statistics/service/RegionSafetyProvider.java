package com.ssafy.backend.domain.statistics.service;

import com.ssafy.backend.domain.safety.entity.RegionSafetyStat;
import com.ssafy.backend.domain.safety.repository.RegionSafetyStatRepository;
import com.ssafy.backend.domain.statistics.dto.response.RegionInfraStatisticsResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionSafetyProvider {

    private static final String SEOUL_SIDO_PREFIX = "11";

    // 안전 인프라 지수 가중치 (합계 1.0)
    // CCTV를 가장 크게, 가로등/경찰시설을 그 다음 비중으로 반영
    private static final double CCTV_WEIGHT = 0.45;
    private static final double STREETLIGHT_WEIGHT = 0.35;
    private static final double POLICE_WEIGHT = 0.20;

    private final RegionSafetyStatRepository regionSafetyStatRepository;

    public RegionInfraStatisticsResponse.RegionSafety getRegionSafety(String sigunguCode) {
        // 1) 전체 지역구 통계를 먼저 읽어 분포(상대 순위) 계산의 기준 집합으로 사용
        List<RegionSafetyStat> allStats = regionSafetyStatRepository.findAll();
        if (allStats.isEmpty()) {
            return emptySafety();
        }

        String normalizedSigunguCode = normalizeSigunguCode(sigunguCode);
        List<RegionSafetyStat> seoulStats = allStats.stream()
                .filter(stat -> isSeoulCode(stat.getSigunguCode()))
                .toList();
        List<RegionSafetyStat> referenceStats = isSeoulCode(normalizedSigunguCode) && !seoulStats.isEmpty()
                ? seoulStats
                : allStats;

        // 2) 요청한 시군구의 통계를 전체 목록에서 찾음
        RegionSafetyStat target = referenceStats.stream()
                .filter(stat -> normalizeSigunguCode(stat.getSigunguCode()).equals(normalizedSigunguCode))
                .findFirst()
                .orElse(null);
        if (target == null) {
            return emptySafety();
        }

        // 3) 면적 보정 밀도(count/area)에 log1p를 적용해 지표 스케일을 안정화
        double targetCctv = logDensity(target.getCctvCount(), target.getAreaKm2());
        double targetStreetlight = logDensity(target.getStreetlightCount(), target.getAreaKm2());
        double targetPolice = logDensity(target.getPoliceFacilityCount(), target.getAreaKm2());

        // 4) 전체 지역구의 지표 분포를 만든 뒤, 대상 지역의 상대 위치 계산
        List<Double> cctvValues = referenceStats.stream()
                .map(stat -> logDensity(stat.getCctvCount(), stat.getAreaKm2()))
                .toList();
        List<Double> streetlightValues = referenceStats.stream()
                .map(stat -> logDensity(stat.getStreetlightCount(), stat.getAreaKm2()))
                .toList();
        List<Double> policeValues = referenceStats.stream()
                .map(stat -> logDensity(stat.getPoliceFacilityCount(), stat.getAreaKm2()))
                .toList();

        double cctvScore = percentileRank(targetCctv, cctvValues);
        double streetlightScore = percentileRank(targetStreetlight, streetlightValues);
        double policeScore = percentileRank(targetPolice, policeValues);

        // 5) 지표별 퍼센타일(0~100)에 가중치를 적용해 최종 안전 인프라 지수 산출
        double compositeScore = (cctvScore * CCTV_WEIGHT)
                + (streetlightScore * STREETLIGHT_WEIGHT)
                + (policeScore * POLICE_WEIGHT);

        // 6) 응답은 소수 1자리로 반올림해 일관된 포맷으로 제공
        return buildResponse(target, roundToOneDecimal(compositeScore));
    }

    // DB 원본 집계값 + 계산된 지수를 RegionSafety 응답 DTO로 변환
    private RegionInfraStatisticsResponse.RegionSafety buildResponse(RegionSafetyStat stat, Double score) {
        return RegionInfraStatisticsResponse.RegionSafety.builder()
                .cctvCount(defaultZero(stat.getCctvCount()))
                .streetLightCount(defaultZero(stat.getStreetlightCount()))
                .securityFacilityCount(defaultZero(stat.getPoliceFacilityCount()))
                .compositeSafetyScore(score)
                .build();
    }

    // 데이터가 없을 때 사용할 기본 응답(모든 값 0)
    private RegionInfraStatisticsResponse.RegionSafety emptySafety() {
        return RegionInfraStatisticsResponse.RegionSafety.builder()
                .cctvCount(0)
                .streetLightCount(0)
                .securityFacilityCount(0)
                .compositeSafetyScore(0.0)
                .build();
    }

    // null 방어용 기본값 처리
    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    // 면적 대비 밀도(count/area)를 계산하고 log1p로 큰 값의 영향력을 완만하게 조정
    private double logDensity(Integer count, Double areaKm2) {
        if (count == null || areaKm2 == null || areaKm2 <= 0) {
            return 0.0;
        }
        return Math.log1p(count / areaKm2);
    }

    // 랭크 계산:
    // target보다 작은 값 개수 + 동일값의 절반을 반영해 0~100 점수로 환산
    private double percentileRank(double target, List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }

        int lower = 0;
        int same = 0;
        for (Double value : values) {
            double current = value == null ? 0.0 : value;
            if (current < target) {
                lower++;
                continue;
            }
            if (Double.compare(current, target) == 0) {
                same++;
            }
        }

        return ((lower + (same * 0.5)) / values.size()) * 100.0;
    }

    // API 응답 가독성을 위해 소수 1자리로 반올림
    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private boolean isSeoulCode(String sigunguCode) {
        String normalized = normalizeSigunguCode(sigunguCode);
        return normalized.startsWith(SEOUL_SIDO_PREFIX);
    }

    private String normalizeSigunguCode(String sigunguCode) {
        return sigunguCode == null ? "" : sigunguCode.trim();
    }
}
