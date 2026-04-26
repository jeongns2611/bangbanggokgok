package com.ssafy.backend.domain.house.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrentHouseListScrollResponse {
    private List<CurrentHouseListResponse.ListElement> data;
    private Boolean hasNext;
    private Long lastId;

    public CurrentHouseListScrollResponse(List<CurrentHouseListResponse.ListElement> data) {
        this.data = data;
    }
}
