package com.ssafy.backend.domain.recommendation.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 지역구 TOP3 추천 조회 요청 DTO
 */
@Getter
@Setter
public class RecommendationRegionTopRequest {

    @NotBlank(message = "sidoName은 필수입니다.")
    private String sidoName;

    @NotEmpty(message = "houseType은 1개 이상 필수입니다.")
    private List<@NotBlank String> houseType;

    @NotBlank(message = "rentType은 필수입니다.")
    private String rentType;

    @NotNull(message = "minDeposit은 필수입니다.")
    @PositiveOrZero(message = "minDeposit은 0 이상이어야 합니다.")
    private Integer minDeposit;

    @NotNull(message = "maxDeposit은 필수입니다.")
    @PositiveOrZero(message = "maxDeposit은 0 이상이어야 합니다.")
    private Integer maxDeposit;

    @NotNull(message = "minMonthlyRent는 필수입니다.")
    @PositiveOrZero(message = "minMonthlyRent는 0 이상이어야 합니다.")
    private Integer minMonthlyRent;

    @NotNull(message = "maxMonthlyRent는 필수입니다.")
    @PositiveOrZero(message = "maxMonthlyRent는 0 이상이어야 합니다.")
    private Integer maxMonthlyRent;

    @NotNull(message = "minFloorSize는 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "minFloorSize는 0보다 커야 합니다.")
    private Double minFloorSize;

    @NotNull(message = "destinationLatitude는 필수입니다.")
    @DecimalMin(value = "-90.0", message = "destinationLatitude는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "destinationLatitude는 90 이하여야 합니다.")
    private Double destinationLatitude;

    @NotNull(message = "destinationLongitude는 필수입니다.")
    @DecimalMin(value = "-180.0", message = "destinationLongitude는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "destinationLongitude는 180 이하여야 합니다.")
    private Double destinationLongitude;

    @AssertTrue(message = "보증금 최소값은 최대값보다 클 수 없습니다.")
    public boolean isDepositRangeValid() {
        if (minDeposit == null || maxDeposit == null) {
            return true;
        }
        return minDeposit <= maxDeposit;
    }

    @AssertTrue(message = "월세 최소값은 최대값보다 클 수 없습니다.")
    public boolean isMonthlyRentRangeValid() {
        if (minMonthlyRent == null || maxMonthlyRent == null) {
            return true;
        }
        return minMonthlyRent <= maxMonthlyRent;
    }

    @AssertTrue(message = "rentType은 전세/월세 또는 JEONSE/MONTHLY만 허용됩니다.")
    public boolean isRentTypeValid() {
        if (rentType == null || rentType.isBlank()) {
            return true;
        }

        String normalized = rentType.trim();
        String upper = normalized.toUpperCase();

        return "전세".equals(normalized)
                || "월세".equals(normalized)
                || "JEONSE".equals(upper)
                || "MONTHLY".equals(upper);
    }

    @AssertTrue(message = "전세인 경우 월세 범위는 0~0이어야 합니다.")
    public boolean isJeonseMonthlyRentValid() {
        if (rentType == null || rentType.isBlank() || minMonthlyRent == null || maxMonthlyRent == null) {
            return true;
        }

        String normalized = rentType.trim();
        String upper = normalized.toUpperCase();
        boolean isJeonse = "전세".equals(normalized) || "JEONSE".equals(upper);

        if (!isJeonse) {
            return true;
        }

        return minMonthlyRent == 0 && maxMonthlyRent == 0;
    }
}
