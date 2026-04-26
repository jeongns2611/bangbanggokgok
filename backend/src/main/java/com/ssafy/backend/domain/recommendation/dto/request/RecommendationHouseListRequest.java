package com.ssafy.backend.domain.recommendation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지역 기반 추천 비교 조회 요청 DTO입니다.
 * 쿼리 파라미터 바인딩을 위해 기본 생성자를 둡니다.
 */
@Getter
@NoArgsConstructor
public class RecommendationHouseListRequest {

    @NotBlank(message = "sidoName은 필수입니다.")
    private String sidoName;

    @NotBlank(message = "sigunguName은 필수입니다.")
    private String sigunguName;
}
