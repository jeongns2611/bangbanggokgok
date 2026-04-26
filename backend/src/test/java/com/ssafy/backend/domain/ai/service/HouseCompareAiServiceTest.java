package com.ssafy.backend.domain.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.backend.domain.ai.dto.response.HouseCompareAiResponse;
import com.ssafy.backend.domain.house.dto.request.HouseCompareAiRequest;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import java.util.List;
import org.junit.jupiter.api.Test;

class HouseCompareAiServiceTest {

    private final HouseCompareAiChatService houseCompareAiChatService = mock(HouseCompareAiChatService.class);
    private final HouseCompareAiService service = new HouseCompareAiService(houseCompareAiChatService);

    @Test
    void normalizeCacheKey_sortsHouseIds() {
        HouseCompareAiRequest request = new HouseCompareAiRequest(List.of(
                houseItem(30L),
                houseItem(10L)
        ));

        String key = HouseCompareAiService.normalizeCacheKey(request, 5L);

        assertThat(key).isEqualTo("5:10,30");
    }

    @Test
    void compare_delegatesToChatServiceWhenRequestIsValid() {
        HouseCompareAiRequest request = new HouseCompareAiRequest(List.of(
                houseItem(1L),
                houseItem(2L)
        ));
        HouseCompareAiResponse expected = new HouseCompareAiResponse("overall", 1L, List.of());
        when(houseCompareAiChatService.generate(request)).thenReturn(expected);

        HouseCompareAiResponse response = service.compare(request, 99L);

        assertThat(response).isSameAs(expected);
        verify(houseCompareAiChatService).generate(request);
    }

    @Test
    void compare_rejectsWhenHouseCountIsNotTwo() {
        HouseCompareAiRequest request = new HouseCompareAiRequest(List.of(houseItem(1L)));

        assertThatThrownBy(() -> service.compare(request, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    void compare_rejectsWhenHouseIdIsMissing() {
        HouseCompareAiRequest request = new HouseCompareAiRequest(List.of(
                houseItem(1L),
                houseItem(null)
        ));

        assertThatThrownBy(() -> service.compare(request, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    private static HouseCompareAiRequest.HouseItem houseItem(Long houseId) {
        return new HouseCompareAiRequest.HouseItem(
                houseId,
                "Yeoksam",
                "ONE_ROOM",
                "MONTHLY",
                1000,
                50,
                18.0,
                "3F",
                "test-house",
                new HouseCompareAiRequest.CommuteData(20, 3.5),
                new HouseCompareAiRequest.InfraCount(1, 2, 3, 4, 5, 6),
                new HouseCompareAiRequest.MinDist(100, 200, 300, 400, 500, 600),
                new HouseCompareAiRequest.DongStats(10, 20, 3, 4.2, 15000, 4500),
                new HouseCompareAiRequest.ScoreSummary(80, 70, 60, 90)
        );
    }
}
