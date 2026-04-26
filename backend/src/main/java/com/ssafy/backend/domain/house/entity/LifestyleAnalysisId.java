package com.ssafy.backend.domain.house.entity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@EqualsAndHashCode
public class LifestyleAnalysisId implements Serializable {
    private Long currentHouse;    // LifestyleAnalysis 엔티티의 필드명과 일치해야 함
    private Long lifestyleCode;   // LifestyleAnalysis 엔티티의 필드명과 일치해야 함
}
