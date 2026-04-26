package com.ssafy.backend.domain.statistics.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * 지역 생활 통계 조회 요청 DTO
 */
public class RegionInfraStatisticsRequest {

   @NotBlank(message = "시/도 이름은 필수 값입니다.")
    private String sidoName;

   @NotBlank(message = "시/군/구 이름은 필수 값입니다.")
    private String sigunguName;
}
