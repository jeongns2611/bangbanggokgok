package com.ssafy.backend.batch.service;

import com.ssafy.backend.domain.ai.dto.request.RagSearchRequest;
import com.ssafy.backend.domain.ai.entity.RagSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final VectorStore vectorStore;

    /**
     * 유저 쿼리와 필터 조건을 기반으로 유사도 검색을 수행합니다.
     *
     * @param query  유저의 자연어 질문 (예: "강남역 근처 조용한 오피스텔 찾아줘")
     * @param filter 필터링 조건 객체
     * @return 검색된 문서 리스트 (유사도 순)
     */
    public List<RagSearchResult> search(String query, RagSearchRequest filter) {
        String filterExpression = buildFilterExpression(filter);

        log.info("RAG Search Query: [{}], Filter: [{}]", query, filterExpression);

        var searchRequestBuilder = SearchRequest.builder()
                .query(query)
                .topK(10);

        if (StringUtils.hasText(filterExpression)) {
            searchRequestBuilder.filterExpression(filterExpression); // 메타데이터 필터링 적용
        }

        SearchRequest searchRequest = searchRequestBuilder.build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        return documents.stream()
                .map(doc -> {
                    Object houseId = doc.getMetadata().get("houseId");
                    Object distance = doc.getMetadata().get("distance"); // 벡터 스토어에 따라 키명이 다를 수 있으나 보통 "distance" 사용
                    log.info("SearchResult -> HouseId: {}, Vector Distance(유사도): {}", houseId, distance);

                    return new RagSearchResult(
                            Long.valueOf(houseId.toString()),
                            doc.getText()
                    );
                })
                .toList();
    }

    public List<RagSearchResult> searchRerankTest(String query, RagSearchRequest filter) {
        BatchService.SementicResults staticResults = BatchService.createStaticResults();

        return staticResults.results().stream()
                .map(ele -> new RagSearchResult(
                        ele.houseId(),
                        ele.chunk()
                ))
                .toList();
    }

    /**
     * RagFilter 객체를 Spring AI Filter Expression 문자열로 변환합니다.
     * 예: "sidoName == '서울' && deposit <= 10000 && monthlyCost <= 50"
     */
    private String buildFilterExpression(RagSearchRequest filter) {
        List<String> expressions = new ArrayList<>();

        // 1. 범주형 필터 (GIN Index 활용)
        if (StringUtils.hasText(filter.getSidoName())) {
            expressions.add("sidoName == '" + filter.getSidoName() + "'");
        }
        if (StringUtils.hasText(filter.getSigunguName())) {
            expressions.add("sigunguName == '" + filter.getSigunguName() + "'");
        }
        if (filter.getHouseType() != null && !filter.getHouseType().isEmpty()) {
            expressions.add("houseType in ['" + String.join("', '", filter.getHouseType()) + "']");
        }
        if (filter.getRentType() != null && !filter.getRentType().isEmpty()) {
            expressions.add("rentType in ['" + String.join("', '", filter.getRentType()) + "']");
        }
        if (filter.getFloor() != null && !filter.getFloor().isEmpty()) {
            expressions.add("floor in ['" + String.join("', '", filter.getFloor()) + "']");
        }

        // 2. 숫자 범위 필터 (B-Tree Index 활용 - Generated Columns)
        // 보증금
        if (filter.getMinDeposit() != null) {
            expressions.add("deposit >= " + filter.getMinDeposit());
        }
        if (filter.getMaxDeposit() != null && filter.getMaxDeposit() >= 0) {
            expressions.add("deposit <= " + filter.getMaxDeposit());
        }

        // 월세
        if (filter.getMinMonthlyCost() != null) {
            expressions.add("monthlyCost >= " + filter.getMinMonthlyCost());
        }
        if (filter.getMaxMonthlyCost() != null && filter.getMaxMonthlyCost() >= 0) {
            expressions.add("monthlyCost <= " + filter.getMaxMonthlyCost());
        }

        // 전용 면적
        if (filter.getMinFloorSize() != null) {
            expressions.add("floorSize >= " + filter.getMinFloorSize());
        }
        if (filter.getMaxFloorSize() != null && filter.getMaxFloorSize() >= 0) {
            expressions.add("floorSize <= " + filter.getMaxFloorSize());
        }

        return String.join(" && ", expressions);
    }

    /**
     * 검색 결과 DTO
     */

}
