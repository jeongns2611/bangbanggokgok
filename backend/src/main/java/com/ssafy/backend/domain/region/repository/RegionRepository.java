package com.ssafy.backend.domain.region.repository;

import com.ssafy.backend.domain.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, String> {
    @Query("""
            SELECT r.regionCode
                FROM Region r
                JOIN r.regionSigungu rg
                JOIN rg.regionSido rs
                WHERE rs.sidoName = :sidoName AND rg.sigunguName = :sigunguName AND r.dongName = :dongName
            """)
    Optional<String> findRegionCodeByNames(
            @Param("sidoName") String sidoName,
            @Param("sigunguName") String sigunguName,
            @Param("dongName") String dongName
    );

    Optional<Region> findByRegionSidoSidoCodeAndRegionSigunguSigunguCodeAndDongName(String sidoCode, String sigunguCode, String dongName);

    @Query("""
            SELECT r
                FROM Region r
                JOIN FETCH r.regionSigungu rg
                JOIN FETCH rg.regionSido rs
            """)
    List<Region> findAllWithSidoAndSigunguFetch();
}
