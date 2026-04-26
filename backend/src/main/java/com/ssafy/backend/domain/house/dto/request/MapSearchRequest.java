package com.ssafy.backend.domain.house.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MapSearchRequest {
    private String sidoName;
    private String sigunguName;
    private List<String> houseType;
    private List<String> rentType;
    private Integer minMonthlyCost;
    private Integer maxMonthlyCost;
    private Integer minDeposit;
    private Integer maxDeposit;
    private Integer managementCost;
    private Double minFloorSize;
    private Double maxFloorSize;
    private List<String> floor;
    private Long lastId;
    private Long pageSize;
}
