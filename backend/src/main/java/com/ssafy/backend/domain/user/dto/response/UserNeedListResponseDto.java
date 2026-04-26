package com.ssafy.backend.domain.user.dto.response;

import java.util.List;

public record UserNeedListResponseDto(
        List<UserNeedConditionDto> conditions
) {
    public record UserNeedConditionDto(
            Long id,
            String name,
            Boolean isDefaultNeed,
            UserNeedSummaryDto userNeed
    ) {
    }

    public record UserNeedSummaryDto(
            String sidoName,
            String sigunguName,
            String targetAddress,
            String rentType,
            Integer depositMax,
            Integer monthlyRentMax,
            List<String> housingTypes,
            List<String> lifestyleTags,
            List<String> floors
    ) {
    }
}
