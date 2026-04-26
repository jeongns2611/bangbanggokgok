package com.ssafy.backend.domain.house.entity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@EqualsAndHashCode
public class HouseFloorId implements Serializable {
    private Long currentHouse;
    private Long houseFloorCode;
}
