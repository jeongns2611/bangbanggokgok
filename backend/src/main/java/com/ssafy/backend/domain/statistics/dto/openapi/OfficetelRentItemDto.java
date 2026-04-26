package com.ssafy.backend.domain.statistics.dto.openapi;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 국토교통부 오피스텔 전월세 실거래가 API의 개별 항목(Item) 정보를 담는 DTO입니다.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfficetelRentItemDto {

    /** 건축년도 */
    @JacksonXmlProperty(localName = "buildYear")
    private Integer buildYear;

    /** 계약기간 */
    @JacksonXmlProperty(localName = "contractTerm")
    private String contractTerm;

    /** 계약구분 (신규/갱신 등) */
    @JacksonXmlProperty(localName = "contractType")
    private String contractType;

    /** 계약일 */
    @JacksonXmlProperty(localName = "dealDay")
    private Integer dealDay;

    /** 계약월 */
    @JacksonXmlProperty(localName = "dealMonth")
    private Integer dealMonth;

    /** 계약년도 */
    @JacksonXmlProperty(localName = "dealYear")
    private Integer dealYear;

    /** 보증금액 (만원) */
    @JacksonXmlProperty(localName = "deposit")
    private String deposit;

    /** 전용면적 (㎡) */
    @JacksonXmlProperty(localName = "excluUseAr")
    private Double excluUseAr;

    /** 층 */
    @JacksonXmlProperty(localName = "floor")
    private Integer floor;

    /** 지번 */
    @JacksonXmlProperty(localName = "jibun")
    private String jibun;

    /** 월세금액 (만원) */
    @JacksonXmlProperty(localName = "monthlyRent")
    private String monthlyRent;

    /** 단지명 */
    @JacksonXmlProperty(localName = "offiNm")
    private String offiNm;

    /** 법정동시군구코드 */
    @JacksonXmlProperty(localName = "sggCd")
    private String sggCd;

    /** 시군구명 */
    @JacksonXmlProperty(localName = "sggNm")
    private String sggNm;

    /** 법정동읍면동명 */
    @JacksonXmlProperty(localName = "umdNm")
    private String umdNm;
}
