-- 1. pgvector 확장 기능을 활성화합니다. (관리자 권한 필요)
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp"; -- UUID 생성을 위해 필요

CREATE TABLE vector_outbox (
    id BIGSERIAL PRIMARY KEY,
    house_id BIGINT NOT NULL,      -- 메인 테이블 ID
    content JSONB,                  -- 임베딩할 매물 조건
    metadata JSONB,                -- 7개의 필터링 조건 (카테고리 등)
    status VARCHAR(20) DEFAULT 'READY', -- READY, PROCESSING, COMPLETED, FAILED, READY_DELETE
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 2. vector_store 테이블을 생성합니다.
-- id: 고유 식별자
-- content: 문서 내용 (텍스트)
-- metadata: 필터링을 위한 메타데이터 (JSONB 형식 권장)
-- embedding: 3072차원 벡터 데이터
CREATE TABLE IF NOT EXISTS vector_store (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    -- 필터링 컬럼 : sidoName, sigunguName, houseType, rentType, floor
    metadata jsonb,
    embedding vector(3072),

    -- 기본값을 false로 설정하여 생성 시 활성 상태로 둡니다.
    is_deleted boolean DEFAULT false NOT NULL,

    -- Java의 metaData 맵 키값(camelCase)과 일치시켜야 합니다.
    -- 값이 들어오면 자동으로 추출되어 저장되며, B-Tree 인덱싱이 가능해집니다.
    deposit int GENERATED ALWAYS AS ((metadata ->> 'deposit')::int) STORED,
    monthly_cost int GENERATED ALWAYS AS ((metadata ->> 'monthlyCost')::int) STORED,
    floor_size float GENERATED ALWAYS AS ((metadata ->> 'floorSize')::float) STORED
);


-- 1. 범주형 필터링용 GIN (지역, 역세권 여부 등)
CREATE INDEX idx_vs_metadata_gin_active ON vector_store
USING gin (metadata jsonb_path_ops)
WHERE (is_deleted = false);

-- 2. 숫자 범위 검색용 B-tree (보증금, 월세, 면적)
CREATE INDEX idx_vs_deposit ON vector_store (deposit) WHERE (is_deleted = false);
CREATE INDEX idx_vs_monthly ON vector_store (monthly_cost) WHERE (is_deleted = false);
CREATE INDEX idx_vs_size ON vector_store (floor_size) WHERE (is_deleted = false);