package com.ssafy.backend.domain.safety.repository;

import com.ssafy.backend.domain.safety.entity.RegionSafetyStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionSafetyStatRepository extends JpaRepository<RegionSafetyStat, String> {
}
