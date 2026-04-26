package com.ssafy.backend.domain.code.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 코드 상세 엔티티.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "common_code_detail",
        indexes = {
                @Index(name = "idx_code_name_group", columnList = "code_name, group_code_id"),
                @Index(name = "idx_group_active_sort", columnList = "group_code_id, is_active, sort_order")
        }
)

public class CommonCodeDetail extends BaseEntity {

    /**
     * 상세 ID (PK)
     * SERIAL
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 그룹 (FK)
     * INTEGER
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_code_id", nullable = false)
    private CommonCodeGroup group;

    /**
     * 코드명
     * VARCHAR(50)
     */
    @Column(name = "code_name", length = 50, nullable = false)
    private String codeName;

    /**
     * 정렬 순서
     * INTEGER
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    /**
     * 활성화 여부
     * BOOLEAN
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
