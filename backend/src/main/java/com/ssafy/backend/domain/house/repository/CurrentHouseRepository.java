package com.ssafy.backend.domain.house.repository;

import com.ssafy.backend.domain.house.entity.CurrentHouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CurrentHouseRepository extends JpaRepository<CurrentHouse, Long>, CurrentHouseRepositoryQuerydsl {
    Page<CurrentHouse> findAllByUserId(Long userId, Pageable pageable);

    @Query(
            value = """
                    SELECT * FROM current_house 
                    WHERE id = ANY(:ids)
                    ORDER BY array_position(:ids, id)
                    """,
            nativeQuery = true
    )
    List<CurrentHouse> findAllByIdsInOrder(@Param("ids") List<Long> ids);

    @Query(
            value = """
                    SELECT *
                    FROM current_house
                    WHERE sido_code = :sidoCode
                      AND sigungu_code = :sigunguCode
                      AND deleted_at IS NULL
                      AND sold_at IS NULL
                    ORDER BY random()
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<CurrentHouse> findRandomActiveHouseBySidoCodeAndSigunguCode(
            @Param("sidoCode") String sidoCode,
            @Param("sigunguCode") String sigunguCode
    );
}
