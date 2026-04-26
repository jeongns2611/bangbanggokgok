package com.ssafy.backend.domain.recommendation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Top10 추천 매물 조회 요청 DTO.
 *
 * GET /api/v1/recommendations/houses/top10 에서 @ModelAttribute로 바인딩한다.
 *
 * [필드 설명]
 *   - id      : UserNeed PK (사용자 매물 조건 ID)
 *               → 이 ID로 사용자가 설정한 보증금/월세/통근시간 등 조건을 DB에서 조회한다.
 *               → 필수(NotNull). 프론트에서 매물 조건 선택 시 전달.
 *
 *   - houseId : 마지막으로 사용자가 조회한 매물 ID (커서 기반 페이징용)
 *               → 기본값 0L = 첫 조회 (처음부터 10건)
 *               → 11번째 이후부터는 이전 페이지의 마지막 매물 ID를 전달하면
 *                 그 다음 위치부터 10건을 반환한다.
 *               → 프론트의 무한 스크롤 UX에서 "nextHouseId"를 여기에 넣어 호출.
 */
@Getter
@Setter
public class RecommendationTop10Request {

    @NotNull(message = "매물 조건(사용자니즈) id는 필수입니다.")
    private Long id;

    private Long houseId = 0L;
}
