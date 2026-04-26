package com.ssafy.backend.domain.house.entity;

import com.ssafy.backend.domain.user.entity.User;
import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wishlist")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(WishlistId.class)
public class Wishlist extends BaseEntity {

    @Id
    @Column(name = "user_id") // DB의 유저 ID 컬럼
    private Long userId;

    @Id
    @Column(name = "house_id") // DB의 실매물 id 컬럼
    private Long houseId;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("houseId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id")
    private CurrentHouse currentHouse;
}
