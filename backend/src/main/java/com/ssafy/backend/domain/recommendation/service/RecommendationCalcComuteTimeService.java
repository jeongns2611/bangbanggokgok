package com.ssafy.backend.domain.recommendation.service;

import com.ssafy.backend.domain.commute.service.CommuteMetricsService;
import com.ssafy.backend.domain.house.entity.CurrentHouse;
import com.ssafy.backend.domain.house.repository.CurrentHouseRepository;
import com.ssafy.backend.domain.region.entity.RegionSigungu;
import com.ssafy.backend.domain.region.repository.RegionSigunguRepository;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationCalcComuteTimeService {

    private static final String SEOUL_SIDO_CODE = "11";

    private final RegionSigunguRepository regionSigunguRepository;
    private final CurrentHouseRepository currentHouseRepository;
    private final CommuteMetricsService commuteMetricsService;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public List<Map<String, Object>> calculateCommuteTimesByDestination(
            Double destinationLatitude,
            Double destinationLongitude
    ) {
        if (destinationLatitude == null || destinationLongitude == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "목적지 위도/경도는 필수입니다.");
        }

        Point destinationPoint = geometryFactory.createPoint(
                new Coordinate(destinationLongitude, destinationLatitude)
        );

        return regionSigunguRepository.findByRegionSidoSidoCode(SEOUL_SIDO_CODE).stream()
                .sorted((left, right) -> left.getSigunguCode().compareTo(right.getSigunguCode()))
                .map(sigungu -> buildDistrictCommuteTime(sigungu, destinationPoint))
                .toList();
    }

    private Map<String, Object> buildDistrictCommuteTime(
            RegionSigungu sigungu,
            Point destinationPoint
    ) {
        // 반환 규칙 
        // 0 이상: 정상 통근시간                                                    
        // -1: 매물 없음 또는 ODsay 계산 불가
        // 검사 방법: ((Integer) map.get("commuteTime")) >= 0
        Integer commuteTime = currentHouseRepository
                .findRandomActiveHouseBySidoCodeAndSigunguCode(SEOUL_SIDO_CODE, sigungu.getSigunguCode())
                .map(CurrentHouse::getPosition)
                .map(housePosition -> commuteMetricsService.getCommuteTimeForTop10(housePosition, destinationPoint))
                .orElse(-1); // 매물이 없거나 ODsay 계산 실패 시 null일 수 있게 유지

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("regionCode", sigungu.getSigunguCode());
        result.put("regionName", sigungu.getSigunguName());
        result.put("commuteTime", commuteTime);
        return result;
    }
}
