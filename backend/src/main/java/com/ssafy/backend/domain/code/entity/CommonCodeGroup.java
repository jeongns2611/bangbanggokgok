package com.ssafy.backend.domain.code.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 코드 그룹 엔티티.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "common_code_group")
public class CommonCodeGroup extends BaseEntity {

    /**
     * 그룹 ID (PK)
     * SERIAL
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 그룹명
     * VARCHAR(50)
     */
    @Column(name = "group_name", length = 50, nullable = false)
    private String groupName;

    /**
     * 설명
     * VARCHAR(255)
     */
    @Column(length = 255)
    private String description;
}
