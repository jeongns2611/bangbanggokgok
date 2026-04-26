-- 지역구별 안전 인프라 지수를 도출할 면적, cctv, 가로등, 경찰서(치안시설) 집계 데이터를 저장할 테이블
CREATE TABLE IF NOT EXISTS region_safety_stat (
    sigungu_code CHAR(5) PRIMARY KEY,
    area_km2 DOUBLE PRECISION NOT NULL,
    cctv_count INTEGER NOT NULL DEFAULT 0,
    streetlight_count INTEGER NOT NULL DEFAULT 0,
    police_facility_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL DEFAULT NOW(),
    created_by BIGINT NOT NULL DEFAULT -1,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT NOW(),
    updated_by BIGINT DEFAULT -1,
    CONSTRAINT fk_region_safety_stat_sigungu
        FOREIGN KEY (sigungu_code)
        REFERENCES region_sigungu(sigungu_code)
);