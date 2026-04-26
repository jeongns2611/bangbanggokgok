package com.ssafy.backend.domain.house.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CurrentHouseRequest {
    private Long houseId;
    private List<houseImage> images;
    private String sidoName;
    private String sigunguName;
    private String dongName;
    private String houseType;
    private String rentType;
    private String floor;
    private String address;
    private Integer deposit;
    private Integer monthlyCost;
    private Integer managementCost;
    private String managementItems;
    private Double floorSize;
    private Integer contractStartYearMonth;
    private Integer contractEndYearMonth;
    private Integer buildYear;
    private String description;

    public record houseImage(
            String objectKey,
            Boolean isThumbnail
    ) {
    }
}

