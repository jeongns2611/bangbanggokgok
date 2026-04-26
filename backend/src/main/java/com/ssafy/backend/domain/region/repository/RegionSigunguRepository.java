package com.ssafy.backend.domain.region.repository;

import com.ssafy.backend.domain.region.entity.RegionSigungu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegionSigunguRepository extends JpaRepository<RegionSigungu, String> {

    @Query("""
            SELECT rg.sigunguCode
                FROM RegionSigungu rg
                JOIN rg.regionSido rs
                WHERE rs.sidoName = :sidoName AND rg.sigunguName = :sigunguName
            """)
    Optional<String> findSigunguCodeByNames(
            @Param("sidoName") String sidoName,
            @Param("sigunguName") String sigunguName
    );

    Optional<RegionSigungu> findByRegionSidoSidoCodeAndSigunguName(String sidoCode, String sigunguName);

    @Query("""
            SELECT rg
                FROM RegionSigungu rg
                JOIN FETCH rg.regionSido
                WHERE rg.sigunguName = :sigunguName
            """)
    List<RegionSigungu> findAllBySigunguName(@Param("sigunguName") String sigunguName);


    @Query("""
            SELECT rg
                FROM RegionSigungu rg
                JOIN FETCH rg.regionSido
            """)
    List<RegionSigungu> findAllWithSidoFetch();

    List<RegionSigungu> findByRegionSidoSidoCode(String sidoCode);
}
