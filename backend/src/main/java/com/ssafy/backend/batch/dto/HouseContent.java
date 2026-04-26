package com.ssafy.backend.batch.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseContent {
    private String houseType;
    private String rentType;
    private String floor;
    private String address;
    private Integer deposit;
    private Integer monthlyCost;
    private Integer managementCost;
    private String managementItems;
    private Double floorSize;
    private Integer buildYear;
}

//[
//        {
//        "houseId" : 123,
//        "description" : "매물에 대한 상세설명입니다"
//        "data" : {
//public class HouseContent {
//    private String houseType; // 오피스텔, 연립/다세대(빌라), 단독/다가구(원룸), 아파트
//    private String rentType; // 전세, 월세
//    private String floor; // 지상층, 반지하, 1층
//    private String address; // 도로명주소
//    private Integer deposit; // 보증금
//    private Integer monthlyCost; // 월세
//    private Integer managementCost; // 관리비
//    private String managementItems; // 관리비항목(전기 등)
//    private Double floorSize; // 전용면적
//    private Integer buildYear; // 건축년도
//}
//        }
//                }
//                ... 총 10개 리스트 요소
//]