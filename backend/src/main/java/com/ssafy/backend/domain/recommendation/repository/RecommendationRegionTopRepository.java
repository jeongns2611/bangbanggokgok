package com.ssafy.backend.domain.recommendation.repository;

import java.util.List;

public interface RecommendationRegionTopRepository {

    List<RecommendationRegionTopHouseRow> findActiveHousesBySido(String sidoCode);

    List<RecommendationRegionTopRow> findRegionStatics(String sidoCode);
}
