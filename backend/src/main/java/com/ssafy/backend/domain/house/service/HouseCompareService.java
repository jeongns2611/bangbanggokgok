package com.ssafy.backend.domain.house.service;

import com.ssafy.backend.domain.house.dto.response.HouseCompareResponse;
import com.ssafy.backend.domain.user.entity.UserNeed;
import com.ssafy.backend.domain.user.entity.UserNeedHouseType;
import com.ssafy.backend.domain.user.repository.UserNeedHouseTypeRepository;
import com.ssafy.backend.domain.user.repository.UserNeedRepository;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HouseCompareService {

    private final UserNeedRepository userNeedRepository;
    private final UserNeedHouseTypeRepository userNeedHouseTypeRepository;
    private final HouseCompareDataAssembler houseCompareDataAssembler;

    /**
     * 기본 사용자 니즈를 기준으로 여러 매물을 비교한다.
     */
    public HouseCompareResponse compare(String ids, Long userId) {
        validateUser(userId);

        UserNeed defaultNeed = userNeedRepository.findByUser_IdAndIsDefaultNeedTrueAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "기본 사용자 니즈를 찾을 수 없습니다."
                ));

        Point targetPos = defaultNeed.getTargetPos();
        if (targetPos == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "기본 사용자 니즈의 목적지 좌표가 없습니다.");
        }

        Set<String> preferredHouseTypes = userNeedHouseTypeRepository
                .findByIdUserNeedIdOrderByCommonCodeDetailSortOrderAsc(defaultNeed.getId()).stream()
                .map(UserNeedHouseType::getCommonCodeDetail)
                .map(commonCodeDetail -> commonCodeDetail.getCodeName().trim())
                .collect(Collectors.toSet());

        List<Long> houseIds = parseHouseIds(ids);
        List<HouseCompareResponse.ComparisonData> comparisonData = houseIds.stream()
                .map(houseId -> houseCompareDataAssembler.assemble(houseId, targetPos, defaultNeed, preferredHouseTypes))
                .toList();

        return new HouseCompareResponse(comparisonData);
    }

    /**
     * 쿼리 파라미터로 전달된 매물 ID 목록을 파싱한다.
     */
    private List<Long> parseHouseIds(String ids) {
        if (ids == null || ids.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "비교할 매물 ID가 필요합니다.");
        }

        try {
            List<Long> houseIds = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(token -> !token.isEmpty())
                    .map(Long::valueOf)
                    .toList();

            if (houseIds.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "비교할 매물 ID가 필요합니다.");
            }

            return houseIds;
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "매물 ID 형식이 올바르지 않습니다.");
        }
    }

    /**
     * 비교 기능은 로그인 사용자를 전제로 한다.
     */
    private void validateUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }
    }
}
