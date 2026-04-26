package com.ssafy.backend.domain.house.dto.response;

import com.ssafy.backend.domain.house.dto.Image;
import com.ssafy.backend.domain.house.entity.CurrentHouse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrentHouseSearchResponse {
    private Long houseId;           // 매물 ID
    private List<Image> images;     // 매물 이미지 리스트
    private Double lat;             // 위도
    private Double lng;             // 경도
    private String sidoName;        // 시이름
    private String sigunguName;     // 구이름
    private String dongName;        // 법정동 이름
    private String houseType;       // 주택유형
    private String rentType;        // 거래유형
    private String houseStatus;     // 매물상태
    private String floor;           // 층수
    private String address;         // 주소
    private Integer deposit;        // 보증금
    private Integer monthlyCost;    // 월세
    private Integer managementCost; // 관리비
    private String managementItems; // 관리비 항목
    private Double floorSize;       // 연면적
    private Integer buildYear;      // 건축년도
    private String description;      // 매물 설명
    private Boolean isLiked;        // 찜 여부
    private CommuteData commuteData;
    private InfraCount infraCount;
    private MinDist minDist;
    private DongStats dongStats;

    public static CurrentHouseSearchResponse from(
            CurrentHouse currentHouse,
            String sidoName,
            String sigunguName,
            String dongName,
            String houseType,
            String rentType,
            String houseStatus,
            String floorName
    ) {
        return new CurrentHouseSearchResponse(
                currentHouse.getId(),
                currentHouse.getImages().stream()
                        .map(image -> new Image(image.getImageUrl(), image.getIsThumbnail()))
                        .toList(),
                currentHouse.getPosition().getY(),
                currentHouse.getPosition().getX(),
                sidoName,
                sigunguName,
                dongName,
                houseType,
                rentType,
                houseStatus,
                floorName,
                currentHouse.getAddress(),
                currentHouse.getDeposit(),
                currentHouse.getMonthlyCost(),
                currentHouse.getManagementCost(),
                currentHouse.getManagementItems(),
                currentHouse.getFloorSize(),
                currentHouse.getBuildYear(),
                currentHouse.getDescription(),
                false,
                null,
                null,
                null,
                null
        );
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommuteData {
        private Integer commuteTime;
        private Double commuteDistance;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InfraCount {
        private Integer convenienceStoreCount;
        private Integer laundryCount;
        private Integer cafeCount;
        private Integer hospitalCount;
        private Integer pharmacyCount;
        private Integer busStopCount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MinDist {
        private Integer convenienceDist;
        private Integer laundryDist;
        private Integer cafeDist;
        private Integer hospitalDist;
        private Integer pharmacyDist;
        private Integer subwayDist;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DongStats {
        private Integer cctvCount;
        private Integer streetLightCount;
        private Integer safetyFacilityCount;
        private Double safetyScore;
        private Integer avgMeatPrice;
        private Integer avgMealPrice;
    }
}
