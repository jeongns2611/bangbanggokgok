package com.ssafy.backend.domain.region.repository;

import com.ssafy.backend.domain.region.entity.RegionStatistic;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 시군구 단위 통계(region_statistic) 조회 전용 JPA 저장소.
 * house compare에서 식비 관련 지역 통계를 가져올 때 사용한다.
 */
public interface RegionStatisticRepository extends JpaRepository<RegionStatistic, String> {
}
