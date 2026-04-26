package com.ssafy.backend.domain.commute.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * API 호출 로그 엔티티.
 * 일별 API 호출 횟수를 누적하여 관리한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "api_call_log")
public class ApiCallLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 기준 날짜. 일별로 하나의 레코드만 존재하도록 unique 제약 조건을 설정한다.
     */
    @Column(name = "reference_date", nullable = false, unique = true)
    private LocalDate referenceDate;

    /**
     * 해당 날짜의 총 호출 횟수.
     */
    @Column(name = "count", nullable = false)
    private Integer count;

    @Builder
    private ApiCallLog(LocalDate referenceDate, Integer count) {
        this.referenceDate = referenceDate;
        this.count = count;
    }

    /**
     * 팩토리 메서드 - 특정 날짜의 초기 호출 로그를 생성한다.
     */
    public static ApiCallLog of(LocalDate referenceDate, Integer count) {
        return ApiCallLog.builder()
                .referenceDate(referenceDate)
                .count(count)
                .build();
    }

    /**
     * 호출 횟수를 1 증가시킨다.
     */
    public void increment() {
        this.count++;
    }
}
