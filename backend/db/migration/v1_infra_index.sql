-- 주변 인프라 조회 API 인덱스 적용
CREATE INDEX IF NOT EXISTS idx_bus_stop_location_geog_gist
    ON bus_stop
    USING GIST ((location::geography));

CREATE INDEX IF NOT EXISTS idx_subway_station_location_geog_gist
    ON subway_station
    USING GIST ((location::geography));

CREATE INDEX IF NOT EXISTS idx_store_location_geog_gist
    ON store
    USING GIST ((location::geography));

CREATE INDEX IF NOT EXISTS idx_store_category_code
    ON store (category_code);

-- 인덱스 생성 후 통계 갱신 (실행 계획 정확도 향상)
ANALYZE bus_stop;
ANALYZE subway_station;
ANALYZE store;

