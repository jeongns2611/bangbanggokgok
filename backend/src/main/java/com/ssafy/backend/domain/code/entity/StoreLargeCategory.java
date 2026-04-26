package com.ssafy.backend.domain.code.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상권 대분류 엔티티.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "store_large_category")
public class StoreLargeCategory extends BaseEntity {

    /**
     * 대분류 코드 (PK)
     * CHAR(2)
     */
    @Id
    @Column(length = 2)
    private String code;

    /**
     * 분류명
     * VARCHAR(20)
     */
    @Column(length = 20, nullable = false)
    private String name;
}
