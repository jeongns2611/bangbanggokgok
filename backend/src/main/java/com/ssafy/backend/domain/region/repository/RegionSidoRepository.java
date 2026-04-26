package com.ssafy.backend.domain.region.repository;

import com.ssafy.backend.domain.region.entity.RegionSido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionSidoRepository extends JpaRepository<RegionSido, String> {
    Optional<RegionSido> findBySidoName(String sidoName);
}
