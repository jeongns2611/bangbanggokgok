package com.ssafy.backend.domain.house.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ToggleWishListResponse {
    Boolean result; // true: 찜 추가, false: 찜 제거
    String message; // "매물 찜하기 성공" 또는 "매물 찜하기 삭제 성공"
}
