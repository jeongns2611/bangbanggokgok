package com.ssafy.backend.domain.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RagSearchRequest {
    private String query = "";
    private String sidoName; // 시도명(예: 서울특별시)
    private String sigunguName; // 구군명(예: 강남구)
    private List<String> houseType; // [오피스텔, 연립/다세대(빌라), 단독/다가구(원룸), 아파트] 다중선택
    private List<String> rentType; // [전세, 월세] 다중선택
    private List<String> floor; // [지상층, 반지하, 1층] 다중선택
    private Integer minDeposit; // 최소 보증금
    private Integer maxDeposit; // 최대 보증금
    private Integer minMonthlyCost; // 최소 월세
    private Integer maxMonthlyCost; // 최대 월세
    private Double minFloorSize; // 최소 전용면적
    private Double maxFloorSize; // 최대 전용면적

    public RagSearchRequest(AiChatRequest request) {
        this.query = request.getQuery();
        this.sidoName = request.getSidoName();
        this.sigunguName = request.getSigunguName();
        this.houseType = request.getHouseType();
        this.rentType = request.getRentType();
        this.floor = request.getFloor();
        this.minDeposit = request.getMinDeposit();
        this.maxDeposit = request.getMaxDeposit();
        this.minMonthlyCost = request.getMinMonthlyCost();
        this.maxMonthlyCost = request.getMaxMonthlyCost();
        this.minFloorSize = request.getMinFloorSize();
        this.maxFloorSize = request.getMaxFloorSize();
    }

    public void update(RagSearchRequest other) {
        if (other.getSidoName() != null) this.sidoName = other.getSidoName();
        if (other.getSigunguName() != null) this.sigunguName = other.getSigunguName();
        if (other.getHouseType() != null) this.houseType = other.getHouseType();
        if (other.getRentType() != null) this.rentType = other.getRentType();
        if (other.getFloor() != null) this.floor = other.getFloor();
        if (other.getMinDeposit() != null) this.minDeposit = other.getMinDeposit();
        if (other.getMaxDeposit() != null) this.maxDeposit = other.getMaxDeposit();
        if (other.getMinMonthlyCost() != null) this.minMonthlyCost = other.getMinMonthlyCost();
        if (other.getMaxMonthlyCost() != null) this.maxMonthlyCost = other.getMaxMonthlyCost();
        if (other.getMinFloorSize() != null) this.minFloorSize = other.getMinFloorSize();
        if (other.getMaxFloorSize() != null) this.maxFloorSize = other.getMaxFloorSize();
    }
}
