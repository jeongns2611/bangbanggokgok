package com.ssafy.backend.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.code.SuccessCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "공통 API 응답 포맷")
public class ApiResponse<T> {

    @Schema(description = "응답 코드", example = "200")
    private final Integer code;

    @Schema(description = "응답 메시지", example = "요청이 성공했습니다.")
    private final String message;

    @Schema(description = "응답 데이터")
    private final T data;

    private ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 성공: 기본 응답
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(
                SuccessCode.SUCCESS.getStatus().value(),
                SuccessCode.SUCCESS.getMessage(),
                null
        );
    }

    // 성공: 데이터만
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                SuccessCode.SUCCESS.getStatus().value(),
                SuccessCode.SUCCESS.getMessage(),
                data
        );
    }

    // 성공: 코드+메시지
    public static ApiResponse<Void> success(SuccessCode successCode) {
        return new ApiResponse<>(
                successCode.getStatus().value(),
                successCode.getMessage(),
                null
        );
    }

    // 성공: 코드+메시지+데이터
    public static <T> ApiResponse<T> success(SuccessCode successCode, T data) {
        return new ApiResponse<>(
                successCode.getStatus().value(),
                successCode.getMessage(),
                data
        );
    }

    // 실패: 기본 응답
    public static ApiResponse<Void> fail() {
        return new ApiResponse<>(
                ErrorCode.FAIL.getStatus().value(),
                ErrorCode.FAIL.getMessage(),
                null
        );
    }

    // 실패: 코드+메시지
    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return new ApiResponse<>(
                errorCode.getStatus().value(),
                errorCode.getMessage(),
                null
        );
    }

    // 실패: 커스텀 메시지
    public static ApiResponse<Void> fail(ErrorCode errorCode, String message) {
        return new ApiResponse<>(
                errorCode.getStatus().value(),
                message,
                null
        );
    }

    // 실패: 코드+메시지+데이터
    public static <T> ApiResponse<T> fail(ErrorCode errorCode, T data) {
        return new ApiResponse<>(
                errorCode.getStatus().value(),
                errorCode.getMessage(),
                data
        );
    }

    // 완전 커스텀 응답
    public static <T> ApiResponse<T> of(Integer code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }
}
