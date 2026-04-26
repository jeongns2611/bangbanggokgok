package com.ssafy.backend.domain.house.entity;

import com.ssafy.backend.domain.code.entity.CommonCodeDetail;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "house_floor")
@Getter
@Setter
@IdClass(HouseFloorId.class)
public class HouseFloor {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("currentHouse")
    @JoinColumn(name = "house_id")
    private CurrentHouse currentHouse;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("houseFloorCode")
    @JoinColumn(name = "floor_id")
    private CommonCodeDetail houseFloorCode;
}