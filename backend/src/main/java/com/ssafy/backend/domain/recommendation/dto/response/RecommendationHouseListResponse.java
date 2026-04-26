package com.ssafy.backend.domain.recommendation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 프론트 비교 화면에서 사용하는 응답 포맷입니다.
 */
@Getter
@Builder
@AllArgsConstructor
public class RecommendationHouseListResponse {

    private List<ComparisonData> comparisonData;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ComparisonData {
        private Long id;
        private Long houseId;
        private String dong;
        private String houseType;
        private String rentType;
        private String houseStatus;
        private Integer deposit;
        private Integer monthlyCost;
        private Integer managementCost;
        private String managementItems;
        private Double floorSize;
        private String floor;
        private List<ImageInfo> images;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageInfo {
        @JsonProperty("image_url")
        private String imageUrl;

        @JsonProperty("is_thumbnail")
        private Boolean isThumbnail;
    }
}
/*
{
  "comparisonData": [
    {
      "id": 501,
      "houseId": 501,
      "dong": "서울특별시 강남구 역삼동",
      "houseType": "단독/다가구",
      "rentType": "월세",
      "houseStatus": "거래가능",
      "deposit": 5000,
      "monthlyCost": 60,
      "managementCost": 10,
      "managementItems": "수도, 인터넷",
      "floorSize": 25.5,
      "floor": "1층",
      "images": [
        {
          "image_url": "https://bucket.s3",
          "is_thumbnail": true
        }
      ]
    }
  ]
}
 */
