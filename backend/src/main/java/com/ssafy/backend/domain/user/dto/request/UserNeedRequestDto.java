package com.ssafy.backend.domain.user.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserNeedRequestDto {

    @NotBlank(message = "조건 이름은 필수입니다.")
    private String needName;

    @NotNull(message = "위도는 필수입니다.")
    private Double lat;

    @NotNull(message = "경도는 필수입니다.")
    private Double lng;

    @NotBlank(message = "목적지 주소는 필수입니다.")
    private String targetAddress;

    @NotBlank(message = "시도 이름은 필수입니다.")
    private String sidoName;

    @NotBlank(message = "시군구 이름은 필수입니다.")
    private String sigunguName;

    @NotNull(message = "최대 허용 통근 시간은 필수입니다.")
    private Integer maxCommuteTime;

    @NotBlank(message = "거래 유형은 필수입니다.")
    private String rentType;

    private Integer maxDeposit;

    private Integer maxMonthlyRent;

    @NotNull(message = "최소 보증금은 필수입니다.")
    private Integer minDeposit;

    @NotNull(message = "최소 월세는 필수입니다.")
    private Integer minMonthlyRent;

    @NotEmpty(message = "주거 유형은 1개 이상 선택해야 합니다.")
    private List<@NotBlank(message = "주거 유형 값은 비어 있을 수 없습니다.") String> housingTypes;

    @NotNull(message = "최소 면적은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "최소 면적은 0보다 커야 합니다.")
    private Double minSize;

    private List<@NotBlank(message = "층 조건 값은 비어 있을 수 없습니다.") String> floors;

    private List<@NotBlank(message = "라이프스타일 태그 값은 비어 있을 수 없습니다.") String> lifestyleTags;
}
