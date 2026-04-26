package com.ssafy.backend.domain.statistics.dto.openapi;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 국토교통부 단독/다가구 전월세 실거래가 API의 개별 항목(Item) 정보를 담는 DTO입니다.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleFamilyRentItemDto {

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

    /** 주택유형 */
    @JacksonXmlProperty(localName = "houseType")
    private String houseType;

    /** 월세금액 (만원) */
    @JacksonXmlProperty(localName = "monthlyRent")
    private String monthlyRent;

    /** 법정동시군구코드 */
    @JacksonXmlProperty(localName = "sggCd")
    private String sggCd;

    /** 계약면적 (㎡) */
    @JacksonXmlProperty(localName = "totalFloorAr")
    private Double totalFloorAr;

    /** 법정동읍면동명 */
    @JacksonXmlProperty(localName = "umdNm")
    private String umdNm;
}
