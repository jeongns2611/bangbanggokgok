package com.ssafy.backend.domain.house.dto.response;

import java.util.List;

public record KakaoAddressResponse(List<Document> documents) {
    public record Document(
            String x,            // 경도 (Longitude)
            String y,            // 위도 (Latitude)
            String address_name  // 전체 지번 주소 또는 도로명 주소
    ) {}
}
