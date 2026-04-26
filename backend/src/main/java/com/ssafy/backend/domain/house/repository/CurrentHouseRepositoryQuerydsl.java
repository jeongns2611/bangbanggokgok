package com.ssafy.backend.domain.house.repository;

import com.ssafy.backend.domain.house.dto.request.MapSearchRequest;
import com.ssafy.backend.domain.house.entity.CurrentHouse;

import java.util.List;

public interface CurrentHouseRepositoryQuerydsl {
    List<CurrentHouse> findAllByFilterCondition(MapSearchRequest req);
}
