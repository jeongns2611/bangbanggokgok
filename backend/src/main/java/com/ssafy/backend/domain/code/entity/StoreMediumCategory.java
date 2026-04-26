package com.ssafy.backend.domain.code.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상권 중분류 엔티티.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "store_medium_category")
public class StoreMediumCategory extends BaseEntity {

    /**
     * 중분류 코드 (PK)
     * CHAR(4)
     */
    @Id
    @Column(length = 4)
    private String code;

    /**
     * 분류명
     * VARCHAR(20)
     */
    @Column(length = 20, nullable = false)
    private String name;

    /**
     * 대분류 (FK)
     * CHAR(2)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "large_category_code", nullable = false)
    private StoreLargeCategory largeCategory;

}
