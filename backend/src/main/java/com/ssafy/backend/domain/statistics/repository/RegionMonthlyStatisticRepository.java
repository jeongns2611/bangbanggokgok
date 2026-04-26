package com.ssafy.backend.domain.statistics.repository;

import com.ssafy.backend.domain.statistics.entity.RegionMonthlyStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * RegionMonthlyStatistic 엔티티에 대한 데이터 액세스를 담당하는 리포지토리 인터페이스입니다.
 */
@Repository
public interface RegionMonthlyStatisticRepository extends JpaRepository<RegionMonthlyStatistic, Long> {
    
    /**
     * 특정 시군구 코드의 월별 통계 데이터를 최신순으로 정렬하여 반환합니다.
     *
     * @param sigunguCode 시군구 코드
     * @return 월별 통계 리스트
     */
    List<RegionMonthlyStatistic> findBySigunguCodeOrderByBaseYearMonthDesc(String sigunguCode);
}
