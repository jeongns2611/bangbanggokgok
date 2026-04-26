package com.ssafy.backend.domain.house.service;

import com.ssafy.backend.domain.commute.dto.request.CommuteRequest;
import com.ssafy.backend.domain.commute.dto.response.OdsayResponse;
import com.ssafy.backend.domain.commute.service.OdsayTransitService;
import com.ssafy.backend.domain.house.dto.response.CurrentHouseResponse;
import com.ssafy.backend.domain.house.dto.response.HouseCompareResponse;
import com.ssafy.backend.domain.infrastructure.dto.response.NearbyInfrastructureResponse;
import com.ssafy.backend.domain.infrastructure.service.InfrastructureService;
import com.ssafy.backend.domain.infrastructure.type.InfrastructureType;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationHouseDetailSummary;
import com.ssafy.backend.domain.recommendation.repository.RecommendationRepository;
import com.ssafy.backend.domain.statistics.service.DongStatsComposer;
import com.ssafy.backend.domain.user.entity.UserNeed;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HouseCompareDataAssembler {

    private final CurrentHouseService currentHouseService;
    private final RecommendationRepository recommendationRepository;
    private final InfrastructureService infrastructureService;
    private final DongStatsComposer dongStatsComposer;
    private final OdsayTransitService odsayTransitService;
    private final HouseCompareScoreCalculator houseCompareScoreCalculator;

    /**
     * 단일 houseId에 대한 비교 응답 데이터를 조립한다.
     */
    public HouseCompareResponse.ComparisonData assemble(
            Long houseId,
            Point targetPos,
            UserNeed userNeed,
            Set<String> preferredHouseTypes
    ) {
        CurrentHouseResponse house = findCurrentHouse(houseId);
        RecommendationHouseDetailSummary detailSummary = recommendationRepository.findHouseDetailById(houseId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.HOUSE_NOT_FOUND,
                        "비교 대상 매물이 존재하지 않습니다. houseId=" + houseId
                ));

        NearbyInfrastructureResponse nearbyInfrastructure = infrastructureService.getNearbyInfrastructures(houseId);
        HouseCompareResponse.DongStats dongStats =
                dongStatsComposer.composeForCompare(detailSummary.getSigunguCode());


        HouseCompareResponse.ComparisonData baseData = new HouseCompareResponse.ComparisonData(
                house.getImages().stream()
                        .map(image -> new HouseCompareResponse.CompareImage(image.getImageUrl(), image.getIsThumbnail()))
                        .toList(),
                house.getHouseId(),
                house.getDongName(),
                house.getHouseType(),
                house.getRentType(),
                house.getHouseStatus(),
                house.getDeposit(),
                house.getMonthlyCost(),
                house.getManagementCost(),
                house.getManagementItems(),
                house.getFloorSize(),
                house.getBuildYear(),
                detailSummary.getDescription(),
                detailSummary.getViewCount(),
                house.getFloor(),
                buildCommuteData(house, targetPos),
                toInfraCount(nearbyInfrastructure),
                toMinDist(nearbyInfrastructure),
                dongStats,
                null
        );

        HouseCompareResponse.ScoreSummary scoreSummary =
                houseCompareScoreCalculator.calculate(baseData, userNeed, preferredHouseTypes);

        return new HouseCompareResponse.ComparisonData(
                baseData.images(),
                baseData.houseId(),
                baseData.dong(),
                baseData.houseType(),
                baseData.rentType(),
                baseData.houseStatus(),
                baseData.deposit(),
                baseData.monthlyCost(),
                baseData.managementCost(),
                baseData.managementItems(),
                baseData.floorSize(),
                baseData.buildYear(),
                baseData.description(),
                baseData.viewCount(),
                baseData.floor(),
                baseData.commuteData(),
                baseData.infraCount(),
                baseData.minDist(),
                baseData.dongStats(),
                scoreSummary
        );
    }

    /**
     * 매물 좌표와 목적지 좌표를 기준으로 최적 통근 경로를 계산한다.
     */
    private List<HouseCompareResponse.CommuteData> buildCommuteData(CurrentHouseResponse house, Point targetPos) {
        Point housePosition = point(house.getLng(), house.getLat());
        CommuteRequest request = new CommuteRequest();
        request.setSX(housePosition.getX());
        request.setSY(housePosition.getY());
        request.setEX(targetPos.getX());
        request.setEY(targetPos.getY());

        OdsayResponse.Path bestPath = extractBestPath(odsayTransitService.getCommuteInfo(request));
        return List.of(new HouseCompareResponse.CommuteData(
                bestPath.getInfo().getTotalTime(),
                roundToOneDecimal(bestPath.getInfo().getTotalDistance() / 1000.0)
        ));
    }

    /**
     * 위도/경도 값을 JTS Point로 변환한다.
     */
    private Point point(Double lng, Double lat) {
        if (lng == null || lat == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "매물 위치 좌표가 없습니다.");
        }
        org.locationtech.jts.geom.GeometryFactory factory =
                new org.locationtech.jts.geom.GeometryFactory(new org.locationtech.jts.geom.PrecisionModel(), 4326);
        return factory.createPoint(new org.locationtech.jts.geom.Coordinate(lng, lat));
    }

    /**
     * ODsay 응답에서 통근 시간이 가장 짧은 경로를 선택한다.
     */
    private OdsayResponse.Path extractBestPath(OdsayResponse response) {
        if (response == null
                || response.getResult() == null
                || response.getResult().getPath() == null
                || response.getResult().getPath().isEmpty()) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "ODsay API에서 유효한 경로를 찾지 못했습니다.");
        }

        return response.getResult().getPath().stream()
                .filter(path -> path.getInfo() != null)
                .min((path1, path2) -> Integer.compare(
                        path1.getInfo().getTotalTime(),
                        path2.getInfo().getTotalTime()
                ))
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.EXTERNAL_API_ERROR,
                        "ODsay API 경로 정보가 비어 있습니다."
                ));
    }

    /**
     * 비교 대상 매물의 기본 정보를 조회한다.
     */
    private CurrentHouseResponse findCurrentHouse(Long houseId) {
        try {
            return currentHouseService.findById(houseId);
        } catch (EntityNotFoundException e) {
            throw new BusinessException(ErrorCode.HOUSE_NOT_FOUND, "비교 대상 매물이 존재하지 않습니다. houseId=" + houseId);
        }
    }

    /**
     * 주변 인프라 요약 결과를 compare 응답의 개수 정보로 변환한다.
     */
    private HouseCompareResponse.InfraCount toInfraCount(NearbyInfrastructureResponse nearbyInfrastructure) {
        Map<String, NearbyInfrastructureResponse.Summary> summaryByType = summaryByType(nearbyInfrastructure);
        return new HouseCompareResponse.InfraCount(
                getCount(summaryByType, InfrastructureType.CONVENIENCE),
                getCount(summaryByType, InfrastructureType.LAUNDRY),
                getCount(summaryByType, InfrastructureType.CAFE),
                getCount(summaryByType, InfrastructureType.HOSPITAL),
                getCount(summaryByType, InfrastructureType.PHARMACY),
                getCount(summaryByType, InfrastructureType.BUS)
        );
    }

    /**
     * 주변 인프라 요약 결과를 compare 응답의 최소 거리 정보로 변환한다.
     */
    private HouseCompareResponse.MinDist toMinDist(NearbyInfrastructureResponse nearbyInfrastructure) {
        Map<String, NearbyInfrastructureResponse.Summary> summaryByType = summaryByType(nearbyInfrastructure);
        return new HouseCompareResponse.MinDist(
                getNearestDistance(summaryByType, InfrastructureType.CONVENIENCE),
                getNearestDistance(summaryByType, InfrastructureType.LAUNDRY),
                getNearestDistance(summaryByType, InfrastructureType.CAFE),
                getNearestDistance(summaryByType, InfrastructureType.HOSPITAL),
                getNearestDistance(summaryByType, InfrastructureType.PHARMACY),
                getNearestDistance(summaryByType, InfrastructureType.SUBWAY)
        );
    }

    /**
     * 인프라 타입별로 요약 데이터를 조회하기 쉽도록 맵으로 변환한다.
     */
    private Map<String, NearbyInfrastructureResponse.Summary> summaryByType(
            NearbyInfrastructureResponse nearbyInfrastructure
    ) {
        if (nearbyInfrastructure == null || nearbyInfrastructure.getSummaries() == null) {
            return Collections.emptyMap();
        }

        return nearbyInfrastructure.getSummaries().stream()
                .collect(Collectors.toMap(
                        NearbyInfrastructureResponse.Summary::getType,
                        Function.identity(),
                        (left, right) -> left
                ));
    }

    /**
     * 해당 인프라 타입의 개수를 반환하며 데이터가 없으면 0을 사용한다.
     */
    private Integer getCount(
            Map<String, NearbyInfrastructureResponse.Summary> summaryByType,
            InfrastructureType type
    ) {
        NearbyInfrastructureResponse.Summary summary = summaryByType.get(type.getCode());
        return summary == null || summary.getCount() == null ? 0 : summary.getCount();
    }

    /**
     * 해당 인프라 타입의 최소 거리를 반환하며 데이터가 없으면 0을 사용한다.
     */
    private Integer getNearestDistance(
            Map<String, NearbyInfrastructureResponse.Summary> summaryByType,
            InfrastructureType type
    ) {
        NearbyInfrastructureResponse.Summary summary = summaryByType.get(type.getCode());
        return summary == null || summary.getNearestDistanceMeters() == null ? 0 : summary.getNearestDistanceMeters();
    }

    /**
     * km 단위 거리를 소수 첫째 자리까지 반올림한다.
     */
    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
