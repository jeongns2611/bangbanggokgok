package com.ssafy.backend.domain.house.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CurrentHouseDeleteResponse {
    Long houseId;
    LocalDateTime deletedAt;
}
