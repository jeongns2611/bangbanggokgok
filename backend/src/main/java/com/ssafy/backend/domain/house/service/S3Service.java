package com.ssafy.backend.domain.house.service;

import com.ssafy.backend.domain.house.dto.response.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public PresignedUrlResponse getPresignedUrl(Long houseId, String fileName, String contentType, Boolean isThumbnail) {
        // 1. 고유한 객체 키 생성 (UUID 활용)
        String objectKey = "uploads/house/" + houseId + "/" + UUID.randomUUID() + "_" + fileName;

        // 2. S3에 업로드할 파일의 메타데이터 설정
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        // 3. Presigned URL 요청 설정 (유효시간 10분)
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        // 4. URL 생성 및 응답 객체 반환
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String url = presignedRequest.url().toString();

        return new PresignedUrlResponse(houseId, url, objectKey, isThumbnail);
    }
}