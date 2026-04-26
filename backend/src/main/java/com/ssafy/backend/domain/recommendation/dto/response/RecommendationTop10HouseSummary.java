package com.ssafy.backend.domain.recommendation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

/**
 * Top10 추천 시 DB에서 조회하는 매물 요약 정보 DTO.
 *
 * RecommendationRepositoryImpl.findTop10HouseSummaries()의 QueryDSL Projections.constructor에서
 * 아래 순서대로 생성자 인자를 넘겨 인스턴스를 생성한다.
 *
 * [사용 흐름]
 *   ① findTop10HouseSummaries()로 전체 활성 매물 조회
 *   ② RecommendationService.buildRankedRecommendations()에서 이 DTO를 순회하며 점수 계산
 *   ③ CachedRecommendationItem으로 변환 → Redis에 캐싱
 *
 * [필드 설명]
 *   - houseId        : 매물 PK (current_house.id)
 *   - dong           : 주소 (concat(시도명 + 시군구명 + 동명))
 *   - houseType      : 주거 형태 코드명 (common_code_detail.code_name)
 *   - rentType       : 임대 유형 코드명 (예: "전세", "월세")
 *   - houseStatus    : 매물 상태 코드명 (예: "거래가능")
 *   - deposit        : 보증금 (만원)
 *   - monthlyCost    : 월세 (만원)
 *   - managementCost : 관리비 (만원)
 *   - managementItems: 관리비 포함 항목 (예: "수도, 인터넷")
 *   - floorSize      : 전용면적 (㎡)
 *   - floor          : 층 정보 코드명 (예: "3층")
 *   - position       : 매물 좌표 (JTS Point, SRID 4326)
 *                      getX()=경도, getY()=위도
 *                      통근 시간 계산 및 프론트 지도 표시에 사용
 */
@Getter
@AllArgsConstructor
public class RecommendationTop10HouseSummary {
    private Long houseId;
    private String dong;
    private String houseType;
    private String rentType;
    private String houseStatus;
    private Integer deposit;
    private Integer monthlyCost;
    private Integer managementCost;
    private String managementItems;
    private Double floorSize;
    private String floor;
    private Point position;
}
