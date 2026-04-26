package com.ssafy.backend.domain.user.dto.response;

import java.util.List;

public record UserNeedDetailResponseDto(
        String name,
        String targetAddress,
        String sidoName,
        String sigunguName,
        Double lat,
        Double lng,
        String rentType,
        Integer minDeposit,
        Integer maxDeposit,
        Integer minMonthlyRent,
        Integer maxMonthlyRent,
        Integer maxCommuteTime,
        Double minExclusiveSize,
        List<String> housingTypes,
        List<String> lifestyleTags,
        List<String> floors
) {
}
