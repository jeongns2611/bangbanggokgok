package com.ssafy.backend.domain.commute.service;

import com.ssafy.backend.domain.commute.dto.request.CommuteRequest;
import com.ssafy.backend.domain.commute.dto.response.OdsayResponse;
import com.ssafy.backend.domain.commute.entity.CommuteTime;
import com.ssafy.backend.domain.commute.repository.CommuteTimeRepository;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통근 지표 서비스.
 * <p>
 * ODsay 대중교통 API 호출과 DB 캐시(commute_time 테이블)를 관리한다.
 * <p>
 * [2단계 캐시 전략]
 * ① DB 캐시(commute_time) 먼저 조회 (ROUND 좌표 비교로 소수점 오차 흡수)
 * → hit: 저장된 totalTime, totalDistance 사용
 * ② DB miss → ODsay 외부 API 호출 → 결과를 DB에 INSERT (다음번 hit용)
 * <p>
 * [버그 ④ 수정] @Transactional(readOnly = true) 아래에서 DB 저장 불가 문제 해결:
 * RecommendationService는 readOnly=true이지만 통근 캐시 저장(save())이 필요하다.
 * 이 서비스를 별도 @Transactional 빈으로 분리하여 쓰기 트랜잭션을 독립적으로 관리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CommuteMetricsService {

    private final CommuteTimeRepository commuteTimeRepository;
    private final OdsayTransitService odsayTransitService;

    /**
     * [매물 상세 조회용] 통근 시간+거리를 조회한다.
     * <p>
     * DB 캐시 hit 시 저장된 시간/거리를 반환하고,
     * miss 시 ODsay API를 호출한 후 결과를 DB에 저장한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CommuteMetrics getOrCreateForDetail(Point housePosition, Point targetPos) {
        CommuteRequest request = buildCommuteRequest(housePosition, targetPos);
        return getOrCreate(housePosition, targetPos, request);
    }

    /**
     * [Top10 전용] 통근 시간만 반환한다. 좌표가 null이거나 ODsay 오류 시 null을 반환한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Integer getCommuteTimeForTop10(Point housePosition, Point targetPos) {
        if (housePosition == null || targetPos == null) {
            return null;
        }
        try {
            CommuteRequest request = buildCommuteRequest(housePosition, targetPos);
            CommuteMetrics metrics = getOrCreate(housePosition, targetPos, request);
            return metrics.commuteTime();
        } catch (BusinessException e) {
            // ODsay 오류 시 해당 매물만 통근 점수 0점으로 처리
            return null;
        }
    }

    /**
     * [MCP 통근 조회용] 통근 시간+거리+환승+비용를 상세히 조회한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public McpCommuteMetrics getOrCreateForMCP(Point housePosition, Point targetPos) {
        CommuteRequest request = buildCommuteRequest(housePosition, targetPos);
        return commuteTimeRepository.findByCoordinates(
                        housePosition.getX(),
                        housePosition.getY(),
                        targetPos.getX(),
                        targetPos.getY()
                )
                .map(this::toMcpCommuteMetrics)
                .orElseGet(() -> fetchAndCacheForMCP(housePosition, targetPos, request));
    }

    /**
     * DB 캐시 조회 → miss 시 ODsay API 호출 및 DB 저장.
     * <p>
     * [버그 ① 수정] DB hit 시 totalDistance를 실제 값으로 반환 (null 아님).
     * CommuteTime 엔티티에 totalDistance 컬럼을 추가하여 해결.
     */
    private CommuteMetrics getOrCreate(Point housePosition, Point targetPos, CommuteRequest request) {
        return commuteTimeRepository.findByCoordinates(
                        housePosition.getX(),  // origin 경도
                        housePosition.getY(),  // origin 위도
                        targetPos.getX(),      // dest 경도
                        targetPos.getY()       // dest 위도
                )
                .map(this::toCommuteMetrics)                                            // DB hit → 캐시 사용
                .orElseGet(() -> fetchAndCache(housePosition, targetPos, request));     // DB miss → API 호출
    }

    /**
     * DB 캐시 miss 시: ODsay API를 호출하고 결과를 commute_time 테이블에 저장한다.
     * <p>
     * [saveIfAbsent 패턴]
     * INSERT 전에 재조회하여 이미 다른 요청이 먼저 저장한 row가 있으면 skip한다.
     * → 동시 요청 시 중복 INSERT 최소화.
     *
     * @throws BusinessException ODsay API에서 경로를 찾지 못한 경우
     */
    @Transactional(readOnly = false)
    public CommuteMetrics fetchAndCache(Point housePosition, Point targetPos, CommuteRequest request) {
        // ODsay API 호출 및 최단시간 경로 선택
        OdsayResponse response = odsayTransitService.getCommuteInfo(request);
        OdsayResponse.Path bestPath = extractBestPath(response);
        OdsayResponse.Info info = bestPath.getInfo();

        // totalDistance: m 단위 → km 변환, 소수점 1자리 반올림
        double distanceKm = roundToOneDecimal(info.getTotalDistance() / 1000.0);

        // saveIfAbsent: INSERT 전에 재조회하여 이미 존재하면 저장하지 않음
        boolean alreadyExists = commuteTimeRepository.findByCoordinates(
                housePosition.getX(), housePosition.getY(),
                targetPos.getX(), targetPos.getY()
        ).isPresent();

        if (!alreadyExists) {
            CommuteTime commuteTime = CommuteTime.of(
                    housePosition,
                    targetPos,
                    (float) info.getTotalTime(),
                    safeTransitCount(info),
                    info.getPayment() == null ? 0 : info.getPayment(),
                    (float) distanceKm  // 신규: totalDistance 저장
            );
            commuteTimeRepository.save(commuteTime);
        }

        // ODsay 응답에서 직접 시간과 거리 반환
        return new CommuteMetrics(info.getTotalTime(), distanceKm);
    }

    /**
     * DB에 저장된 CommuteTime 엔티티 → CommuteMetrics 변환.
     * <p>
     * [버그 ① 수정] totalDistance 컬럼이 추가되어 DB hit 시에도 정확한 거리를 반환한다.
     * 기존 레코드(totalDistance = null)는 null로 유지된다.
     */
    private CommuteMetrics toCommuteMetrics(CommuteTime commuteTime) {
        Integer time = commuteTime.getTotalTime() == null
                ? null
                : Math.round(commuteTime.getTotalTime());

        Double distance = commuteTime.getTotalDistance() == null
                ? null
                : (double) commuteTime.getTotalDistance();

        return new CommuteMetrics(time, distance);
    }

    /**
     * DB에 저장된 CommuteTime 엔티티 → McpCommuteMetrics 변환.
     */
    private McpCommuteMetrics toMcpCommuteMetrics(CommuteTime commuteTime) {
        Integer time = commuteTime.getTotalTime() == null
                ? null
                : Math.round(commuteTime.getTotalTime());

        Double distance = commuteTime.getTotalDistance() == null
                ? null
                : (double) commuteTime.getTotalDistance();

        return new McpCommuteMetrics(time, distance, commuteTime.getTransitCount(), commuteTime.getCost());
    }

    // ─────────────────────────────────────────────────────────────
    //  private 내부 구현
    // ─────────────────────────────────────────────────────────────

    /**
     * DB 캐시 miss 시: ODsay API를 호출하고 결과를 commute_time 테이블에 저장한다.
     * <p>
     * [MCP 전용] 통근 시간, 거리, 환승 횟수, 비용을 모두 저장한다.
     */
    @Transactional(readOnly = false)
    public McpCommuteMetrics fetchAndCacheForMCP(Point housePosition, Point targetPos, CommuteRequest request) {
        OdsayResponse response = odsayTransitService.getCommuteInfo(request);
        OdsayResponse.Path bestPath = extractBestPath(response);
        OdsayResponse.Info info = bestPath.getInfo();

        double distanceKm = roundToOneDecimal(info.getTotalDistance() / 1000.0);
        int transitCount = safeTransitCount(info);
        int cost = info.getPayment() == null ? 0 : info.getPayment();

        boolean alreadyExists = commuteTimeRepository.findByCoordinates(
                housePosition.getX(), housePosition.getY(),
                targetPos.getX(), targetPos.getY()
        ).isPresent();

        if (!alreadyExists) {
            CommuteTime commuteTime = CommuteTime.of(
                    housePosition,
                    targetPos,
                    (float) info.getTotalTime(),
                    transitCount,
                    cost,
                    (float) distanceKm
            );
            commuteTimeRepository.save(commuteTime);
        }

        return new McpCommuteMetrics(info.getTotalTime(), distanceKm, transitCount, cost);
    }

    /**
     * ODsay 응답에서 최단 시간 경로(best path)를 추출한다.
     * Path.info.totalTime 기준으로 가장 짧은 경로를 선택한다.
     *
     * @throws BusinessException 경로가 없거나 응답이 비어있는 경우
     */
    private OdsayResponse.Path extractBestPath(OdsayResponse response) {
        if (response == null
                || response.getResult() == null
                || response.getResult().getPath() == null
                || response.getResult().getPath().isEmpty()) {
            throw new BusinessException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    "ODsay API에서 통근 경로를 찾지 못했습니다."
            );
        }

        // 여러 경로(지하철 only, 버스 only, 버스+지하철) 중 totalTime 최소 경로 선택
        return response.getResult().getPath().stream()
                .filter(path -> path.getInfo() != null)
                .min((p1, p2) -> Integer.compare(
                        p1.getInfo().getTotalTime(),
                        p2.getInfo().getTotalTime()
                ))
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.EXTERNAL_API_ERROR,
                        "ODsay API 응답에 경로 정보가 없습니다."
                ));
    }

    /**
     * ODsay 응답에서 환승 횟수를 안전하게 추출한다.
     * 버스 환승 + 지하철 환승 합산. null이면 0으로 처리.
     */
    private Integer safeTransitCount(OdsayResponse.Info info) {
        int bus = info.getBusTransitCount() == null ? 0 : info.getBusTransitCount();
        int subway = info.getSubwayTransitCount() == null ? 0 : info.getSubwayTransitCount();
        return bus + subway;
    }

    /**
     * ODsay 요청 DTO를 생성한다.
     * Point.getX() = 경도(longitude), Point.getY() = 위도(latitude)
     * ODsay 좌표계: SX/EX = 경도, SY/EY = 위도
     *
     * @param housePosition 출발지(매물) 좌표
     * @param targetPos     도착지(직장) 좌표
     */
    private CommuteRequest buildCommuteRequest(Point housePosition, Point targetPos) {
        CommuteRequest request = new CommuteRequest();
        request.setSX(housePosition.getX());  // 출발 경도 (매물)
        request.setSY(housePosition.getY());  // 출발 위도 (매물)
        request.setEX(targetPos.getX());      // 도착 경도 (직장)
        request.setEY(targetPos.getY());      // 도착 위도 (직장)
        return request;
    }

    /**
     * 소수점 첫째 자리까지 반올림한다. 예: 12.456 → 12.5
     */
    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    /**
     * 통근 시간과 거리(상세 조회용)를 반환하는 내부 DTO (record).
     *
     * @param commuteTime     통근 시간 (분). null이면 정보 없음.
     * @param commuteDistance 통근 거리 (km, 소수점 1자리). null이면 정보 없음.
     */
    public record CommuteMetrics(Integer commuteTime, Double commuteDistance) {
    }

    /**
     * MCP용 상세 통근 정보.
     */
    public record McpCommuteMetrics(Integer totalTime, Double totalDistance, Integer transitCount, Integer cost) {
    }
}
