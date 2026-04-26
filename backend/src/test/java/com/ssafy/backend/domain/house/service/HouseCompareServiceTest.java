package com.ssafy.backend.domain.house.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.backend.domain.house.dto.response.HouseCompareResponse;
import com.ssafy.backend.domain.user.entity.UserNeed;
import com.ssafy.backend.domain.user.repository.UserNeedHouseTypeRepository;
import com.ssafy.backend.domain.user.repository.UserNeedRepository;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

class HouseCompareServiceTest {

    private final UserNeedRepository userNeedRepository = mock(UserNeedRepository.class);
    private final UserNeedHouseTypeRepository userNeedHouseTypeRepository = mock(UserNeedHouseTypeRepository.class);
    private final HouseCompareDataAssembler houseCompareDataAssembler = mock(HouseCompareDataAssembler.class);

    private final HouseCompareService service = new HouseCompareService(
            userNeedRepository,
            userNeedHouseTypeRepository,
            houseCompareDataAssembler
    );

    @Test
    void compare_buildsComparisonDataViaAssembler() {
        UserNeed defaultNeed = UserNeed.builder()
                .targetAddress("Gangnam")
                .targetPos(point(127.0276, 37.4979))
                .maxCommuteTime(30)
                .rentType("MONTHLY")
                .minDeposit(1000)
                .minMonthlyRent(40)
                .minExclusiveSize("18")
                .isDefaultNeed(true)
                .build();
        when(userNeedRepository.findByUser_IdAndIsDefaultNeedTrueAndDeletedAtIsNull(7L))
                .thenReturn(Optional.of(defaultNeed));
        when(userNeedHouseTypeRepository.findByIdUserNeedIdOrderByCommonCodeDetailSortOrderAsc(null))
                .thenReturn(List.of());

        HouseCompareResponse.ComparisonData first = sampleComparisonData(101L);
        HouseCompareResponse.ComparisonData second = sampleComparisonData(202L);
        when(houseCompareDataAssembler.assemble(101L, defaultNeed.getTargetPos(), defaultNeed, java.util.Set.of()))
                .thenReturn(first);
        when(houseCompareDataAssembler.assemble(202L, defaultNeed.getTargetPos(), defaultNeed, java.util.Set.of()))
                .thenReturn(second);

        HouseCompareResponse response = service.compare("101,202", 7L);

        assertThat(response.comparisonData()).containsExactly(first, second);
        verify(houseCompareDataAssembler).assemble(101L, defaultNeed.getTargetPos(), defaultNeed, java.util.Set.of());
        verify(houseCompareDataAssembler).assemble(202L, defaultNeed.getTargetPos(), defaultNeed, java.util.Set.of());
    }

    @Test
    void compare_rejectsAnonymousUser() {
        assertThatThrownBy(() -> service.compare("1,2", null))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    void compare_rejectsInvalidHouseIds() {
        UserNeed defaultNeed = UserNeed.builder()
                .targetAddress("Gangnam")
                .targetPos(point(127.0276, 37.4979))
                .maxCommuteTime(30)
                .rentType("MONTHLY")
                .minDeposit(1000)
                .minMonthlyRent(40)
                .minExclusiveSize("18")
                .isDefaultNeed(true)
                .build();
        when(userNeedRepository.findByUser_IdAndIsDefaultNeedTrueAndDeletedAtIsNull(7L))
                .thenReturn(Optional.of(defaultNeed));

        assertThatThrownBy(() -> service.compare("1,a", 7L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    private static Point point(double lng, double lat) {
        return new GeometryFactory().createPoint(new Coordinate(lng, lat));
    }

    private static HouseCompareResponse.ComparisonData sampleComparisonData(Long houseId) {
        return new HouseCompareResponse.ComparisonData(
                List.of(),
                houseId,
                "Seoul Gangnam",
                "ONE_ROOM",
                "MONTHLY",
                "AVAILABLE",
                1000,
                60,
                5,
                "internet",
                18.5,
                2020,
                "test house",
                10,
                "3F",
                List.of(new HouseCompareResponse.CommuteData(20, 3.4)),
                new HouseCompareResponse.InfraCount(1, 1, 1, 1, 1, 1),
                new HouseCompareResponse.MinDist(100, 200, 300, 400, 500, 600),
                new HouseCompareResponse.DongStats(0, 0, 0, 0.0, 10000, 4000),
                new HouseCompareResponse.ScoreSummary(80, 70, 60, 75)
        );
    }
}
