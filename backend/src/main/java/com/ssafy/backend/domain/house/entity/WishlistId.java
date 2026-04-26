package com.ssafy.backend.domain.house.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WishlistId implements Serializable {
    private Long userId;  // Wishlist 내 @MapsId("userId")와 매핑
    private Long houseId; // Wishlist 내 @MapsId("houseId")와 매핑
}