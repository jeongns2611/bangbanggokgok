package com.ssafy.backend.domain.house.entity;

import com.ssafy.backend.domain.code.entity.CommonCodeDetail;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lifestyle_analysis")
@Getter @Setter
@IdClass(LifestyleAnalysisId.class)
public class LifestyleAnalysis {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("currentHouse") // LifestyleAnalysisId의 currentHouse 필드와 매핑
    @JoinColumn(name = "house_id")
    private CurrentHouse currentHouse;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("lifestyleCode") // LifestyleAnalysisId의 lifestyleCode 필드와 매핑
    @JoinColumn(name = "lifestyle_id")
    private CommonCodeDetail lifestyleCode;
}
