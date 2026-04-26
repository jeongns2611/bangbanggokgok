package com.ssafy.backend.domain.house.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Image {
    private String imageUrl;     // S3 이미지 URL
    private Boolean isThumbnail; // 썸네일 여부
}
