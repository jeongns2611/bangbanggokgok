package com.ssafy.backend.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScoredMcpHouseInfo {
    Long houseId;
    Double score;
    String coordinates;
    String content;
}
