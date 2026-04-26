package com.ssafy.backend.domain.house.repository;

import com.ssafy.backend.domain.house.entity.CurrentHouse;
import com.ssafy.backend.domain.house.entity.Wishlist;
import com.ssafy.backend.domain.house.entity.WishlistId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface WishListRepository extends JpaRepository<Wishlist, WishlistId> {
    @Query("""
            SELECT DISTINCT ch
                FROM Wishlist w
                JOIN w.currentHouse ch
                LEFT JOIN FETCH ch.images
                WHERE w.userId = :userId
            """)
    List<CurrentHouse> findAllWishListByUserId(@Param("userId") Long userId);

    Boolean existsByUserIdAndHouseId(Long userId, Long houseId);

    @Query("SELECT w.houseId FROM Wishlist w WHERE w.userId = :userId AND w.houseId IN :houseIds")
    Set<Long> findLikedHouseIdsByUserIdAndHouseIds(@Param("userId") Long userId, @Param("houseIds") List<Long> houseIds);

    void deleteByHouseId(Long houseId);

    @Query("SELECT w.currentHouse FROM Wishlist w WHERE w.userId = :userId ORDER BY w.createdAt DESC")
    Page<CurrentHouse> findWishListHousesByUserIdWithPaging(@Param("userId") Long userId, Pageable pageable);
}
