package com.ssafy.backend.domain.house.dto.response;

import com.ssafy.backend.domain.house.entity.CurrentHouse;
import com.ssafy.backend.domain.house.entity.HouseImage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CurrentHouseListResponse {
    List<ListElement> data;
    private Integer totalPages = 0;

    public CurrentHouseListResponse(List<ListElement> data) {
        this.data = data;
    }

    public CurrentHouseListResponse(List<ListElement> data, Integer totalPages) {
        this.data = data;
        this.totalPages = totalPages;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListElement {
        private Long houseId;
        private String thumbnailUrl;
        private Double lat;
        private Double lng;
        private String sidoName;
        private String sigunguName;
        private String dongName;
        private String houseType;
        private String rentType;
        private String houseStatus;
        private String floor;
        private Integer deposit;
        private Integer monthlyCost;
        private Boolean isLiked;

        public static ListElement from(
                CurrentHouse currentHouse,
                String sidoName,
                String sigunguName,
                String dongName,
                String houseType,
                String rentType,
                String houseStatus,
                String floor
        ) {
            String thumbnailUrl = currentHouse.getImages().stream()
                    .filter(image -> Boolean.TRUE.equals(image.getIsThumbnail()))
                    .findFirst()
                    .map(HouseImage::getImageUrl)
                    .orElse(null);

            return new ListElement(
                    currentHouse.getId(),
                    thumbnailUrl,
                    currentHouse.getPosition().getY(),
                    currentHouse.getPosition().getX(),
                    sidoName,
                    sigunguName,
                    dongName,
                    houseType,
                    rentType,
                    houseStatus,
                    floor,
                    currentHouse.getDeposit(),
                    currentHouse.getMonthlyCost(),
                    false
            );
        }
    }

}
