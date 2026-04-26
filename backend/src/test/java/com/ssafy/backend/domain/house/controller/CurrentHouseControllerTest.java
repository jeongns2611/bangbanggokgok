package com.ssafy.backend.domain.house.controller;

import com.ssafy.backend.domain.ai.dto.response.HouseCompareAiResponse;
import com.ssafy.backend.domain.ai.service.HouseCompareAiService;
import com.ssafy.backend.domain.house.dto.request.HouseCompareAiRequest;
import com.ssafy.backend.domain.house.dto.response.HouseCompareResponse;
import com.ssafy.backend.domain.house.service.CurrentHouseService;
import com.ssafy.backend.domain.house.service.HouseCompareService;
import com.ssafy.backend.domain.house.service.S3Service;
import com.ssafy.backend.global.auth.principal.CustomOAuth2User;
import com.ssafy.backend.global.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CurrentHouseControllerTest {

    private final CurrentHouseService currentHouseService = mock(CurrentHouseService.class);
    private final HouseCompareService houseCompareService = mock(HouseCompareService.class);
    private final HouseCompareAiService houseCompareAiService = mock(HouseCompareAiService.class);
    private final S3Service s3Service = mock(S3Service.class);

    private final CurrentHouseController controller = new CurrentHouseController(
            currentHouseService,
            houseCompareService,
            houseCompareAiService,
            s3Service
    );

    private static CustomOAuth2User user(Long userId) {
        return new CustomOAuth2User(
                userId,
                AuthorityUtils.NO_AUTHORITIES,
                Map.of(),
                "id",
                "test@example.com",
                "tester",
                null,
                false
        );
    }

    private static HouseCompareAiRequest.HouseItem houseItem(Long houseId, String dong) {
        return new HouseCompareAiRequest.HouseItem(
                houseId,
                dong,
                "ONE_ROOM",
                "MONTHLY",
                1000,
                50,
                18.5,
                "3F",
                "test-house",
                new HouseCompareAiRequest.CommuteData(25, 4.2),
                new HouseCompareAiRequest.InfraCount(1, 1, 1, 1, 1, 1),
                new HouseCompareAiRequest.MinDist(100, 200, 300, 400, 500, 600),
                new HouseCompareAiRequest.DongStats(10, 20, 5, 4.0, 15000, 4500),
                new HouseCompareAiRequest.ScoreSummary(80, 70, 75, 85)
        );
    }

    @Test
    void compareHouses_passesIdsAndAuthenticatedUserIdToService() {
        HouseCompareResponse expected = new HouseCompareResponse(List.of());
        when(houseCompareService.compare("1,2", 7L)).thenReturn(expected);

        ApiResponse<?> response = controller.compareHouses("1,2", user(7L));

        assertThat(response.getData()).isSameAs(expected);
        verify(houseCompareService).compare("1,2", 7L);
    }

    @Test
    void compareHouses_passesNullUserIdWhenAnonymous() {
        HouseCompareResponse expected = new HouseCompareResponse(List.of());
        when(houseCompareService.compare("3,4", null)).thenReturn(expected);

        ApiResponse<?> response = controller.compareHouses("3,4", null);

        assertThat(response.getData()).isSameAs(expected);
        verify(houseCompareService).compare("3,4", null);
    }

    @Test
    void compareHousesByAi_passesRequestAndAuthenticatedUserIdToService() {
        HouseCompareAiRequest request = new HouseCompareAiRequest(List.of(
                houseItem(10L, "A"),
                houseItem(20L, "B")
        ));
        HouseCompareAiResponse expected = new HouseCompareAiResponse(
                "overall",
                10L,
                List.of(new HouseCompareAiResponse.HouseEvaluation(10L, "A", List.of("good"), List.of()))
        );
        when(houseCompareAiService.compare(request, 9L)).thenReturn(expected);

        ApiResponse<?> response = controller.compareHousesByAi(request, user(9L));

        assertThat(response.getData()).isSameAs(expected);
        verify(houseCompareAiService).compare(request, 9L);
    }

    @Test
    void compareHousesByAi_passesNullUserIdWhenAnonymous() {
        HouseCompareAiRequest request = new HouseCompareAiRequest(List.of(
                houseItem(11L, "A"),
                houseItem(22L, "B")
        ));
        HouseCompareAiResponse expected = new HouseCompareAiResponse("overall", 3L, List.of());
        when(houseCompareAiService.compare(any(HouseCompareAiRequest.class), any())).thenReturn(expected);

        ApiResponse<?> response = controller.compareHousesByAi(request, null);

        assertThat(response.getData()).isSameAs(expected);
        verify(houseCompareAiService).compare(request, null);
    }
}
