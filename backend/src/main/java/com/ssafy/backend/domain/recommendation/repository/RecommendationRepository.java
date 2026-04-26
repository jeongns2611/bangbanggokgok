package com.ssafy.backend.domain.recommendation.repository;

import java.util.List;
import java.util.Optional;

import com.ssafy.backend.domain.recommendation.dto.response.RecommendationHouseDetailResponse;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationHouseDetailSummary;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationHouseImageRow;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationHouseListSummary;
import com.ssafy.backend.domain.recommendation.dto.response.RecommendationTop10HouseSummary;

public interface RecommendationRepository {

    List<RecommendationHouseListSummary> findHouseSummariesByRegion(String sidoCode, String sigunguCode);

    List<RecommendationHouseImageRow> findHouseImagesByHouseIds(List<Long> houseIds);

    Optional<RecommendationHouseDetailSummary> findHouseDetailById(Long houseId);

    List<RecommendationHouseImageRow> findHouseImagesByHouseId(Long houseId);

    RecommendationHouseDetailResponse.InfraCount findInfraCountByHouseId(Long houseId);

    RecommendationHouseDetailResponse.MinDist findMinDistByHouseId(Long houseId);

    RecommendationHouseDetailResponse.DongStats findDongStatsByHouseId(Long houseId);

    List<RecommendationTop10HouseSummary> findTop10HouseSummaries(String sidoName, String sigunguName);
}
