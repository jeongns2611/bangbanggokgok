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
public class AiChatRequest {
    private String query;
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
}