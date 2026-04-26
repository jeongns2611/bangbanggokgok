package com.ssafy.backend.domain.commute.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommuteRequest {

    @NotNull(message = "출발지 X 좌표는 필수입니다.")
    private Double SX;

    @NotNull(message = "출발지 Y 좌표는 필수입니다.")
    private Double SY;

    @NotNull(message = "도착지 X 좌표는 필수입니다.")
    private Double EX;

    @NotNull(message = "도착지 Y 좌표는 필수입니다.")
    private Double EY;

    private Integer opt = 0;

    private Integer searchType = 0;

    private Integer searchPathType = 0;
}
