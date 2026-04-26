package com.ssafy.backend.domain.statistics.dto.openapi;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 국토교통부 OpenAPI 전월세 실거래가 응답을 담는 공용 DTO 클래스입니다.
 * XML 형식의 응답 구조를 Java 객체로 매핑합니다.
 * 
 * @param <T> 상세 항목(item)의 타입 (단독다가구 또는 오피스텔 DTO)
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@JacksonXmlRootElement(localName = "response")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenApiResponseDto<T> {

    /** 응답 헤더 정보 (결과 코드, 메시지 등) */
    @JacksonXmlProperty(localName = "header")
    private Header header;

    /** 응답 바디 정보 (데이터 목록, 페이징 정보 등) */
    @JacksonXmlProperty(localName = "body")
    private Body<T> body;

    /**
     * API 호출 결과 상태 정보를 담는 내부 클래스입니다.
     */
    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        /** 결과 코드 (00: 정상, 그 외 에러) */
        @JacksonXmlProperty(localName = "resultCode")
        private String resultCode;

        /** 결과 메시지 */
        @JacksonXmlProperty(localName = "resultMsg")
        private String resultMsg;
    }

    /**
     * 실제 데이터 목록과 페이징 정보를 담는 내부 클래스입니다.
     */
    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body<T> {
        /** 데이터 목록 */
        @JacksonXmlElementWrapper(localName = "items")
        @JacksonXmlProperty(localName = "item")
        private List<T> items;

        /** 한 페이지 결과 수 */
        @JacksonXmlProperty(localName = "numOfRows")
        private Integer numOfRows;

        /** 페이지 번호 */
        @JacksonXmlProperty(localName = "pageNo")
        private Integer pageNo;

        /** 전체 결과 수 */
        @JacksonXmlProperty(localName = "totalCount")
        private Integer totalCount;
    }
}
