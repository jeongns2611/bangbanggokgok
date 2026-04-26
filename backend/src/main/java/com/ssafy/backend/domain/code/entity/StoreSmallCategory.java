package com.ssafy.backend.domain.code.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상권 소분류 엔티티.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "store_small_category")
public class StoreSmallCategory extends BaseEntity {

    /**
     * 소분류 코드 (PK)
     * CHAR(6)
     */
    @Id
    @Column(length = 6)
    private String code;

    /**
     * 소분류명
     * VARCHAR(30)
     */
    @Column(length = 30, nullable = false)
    private String name;

    /**
     * 중분류 (FK)
     * CHAR(4)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medium_category_code", nullable = false)
    private StoreMediumCategory mediumCategory;
}
