package com.ssafy.backend.domain.user.service;

import com.ssafy.backend.domain.code.entity.CommonCodeDetail;
import com.ssafy.backend.domain.code.repository.CommonCodeDetailRepository;
import com.ssafy.backend.domain.user.dto.request.UserNeedRequestDto;
import com.ssafy.backend.domain.user.dto.response.*;
import com.ssafy.backend.domain.user.entity.*;
import com.ssafy.backend.domain.user.repository.*;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserNeedService {

    private static final Long HOUSE_TYPE_GROUP_ID = 1L;
    private static final Long LIFESTYLE_TAG_GROUP_ID = 6L;
    private static final Long FLOOR_GROUP_ID = 9L;

    private final UserRepository userRepository;
    private final UserNeedRepository userNeedRepository;
    private final CommonCodeDetailRepository commonCodeDetailRepository;
    private final UserNeedHouseTypeRepository userNeedHouseTypeRepository;
    private final UserNeedLifestyleTagRepository userNeedLifestyleTagRepository;
    private final UserNeedFloorRepository userNeedFloorRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public UserNeedCreateResponseDto createUserNeed(Long userId, UserNeedRequestDto request) {
        validateRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        boolean hasExistingUserNeed = userNeedRepository.existsByUser_IdAndDeletedAtIsNull(userId);

        UserNeed userNeed = userNeedRepository.save(
                UserNeed.builder()
                        .user(user)
                        .needName(normalizeNeedName(request.getNeedName()))
                        .targetAddress(request.getTargetAddress())
                        .targetPos(createTargetPoint(request))
                        .sidoName(normalizeRegionName(request.getSidoName(), "sidoName"))
                        .sigunguName(normalizeRegionName(request.getSigunguName(), "sigunguName"))
                        .maxCommuteTime(request.getMaxCommuteTime())
                        .rentType(normalizeRentType(request.getRentType()))
                        .minDeposit(request.getMinDeposit())
                        .maxDeposit(request.getMaxDeposit())
                        .minMonthlyRent(request.getMinMonthlyRent())
                        .maxMonthlyRent(request.getMaxMonthlyRent())
                        .minExclusiveSize(String.valueOf(request.getMinSize()))
                        .isDefaultNeed(!hasExistingUserNeed)
                        .build()
        );

        saveHouseTypes(userNeed, request.getHousingTypes());
        saveLifestyleTags(userNeed, request.getLifestyleTags());
        saveFloors(userNeed, request.getFloors());

        return new UserNeedCreateResponseDto(userNeed.getId());
    }

    @Transactional
    public UserNeedUpdateResponseDto updateUserNeed(Long userId, Long userNeedId, UserNeedRequestDto request) {
        validateRequest(request);

        UserNeed userNeed = getActiveUserNeed(userId, userNeedId);
        userNeed.update(
                normalizeNeedName(request.getNeedName()),
                request.getTargetAddress(),
                createTargetPoint(request),
                normalizeRegionName(request.getSidoName(), "sidoName"),
                normalizeRegionName(request.getSigunguName(), "sigunguName"),
                request.getMaxCommuteTime(),
                normalizeRentType(request.getRentType()),
                request.getMinDeposit(),
                request.getMaxDeposit(),
                request.getMinMonthlyRent(),
                request.getMaxMonthlyRent(),
                String.valueOf(request.getMinSize())
        );

        replaceHouseTypes(userNeed, request.getHousingTypes());
        replaceLifestyleTags(userNeed, request.getLifestyleTags());
        replaceFloors(userNeed, request.getFloors());

        return new UserNeedUpdateResponseDto(
                userNeed.getId(),
                userNeed.getUpdatedAt(),
                buildUserNeedDetailResponse(userNeed)
        );
    }

    @Transactional
    public void deleteUserNeed(Long userId, Long userNeedId) {
        UserNeed userNeed = getActiveUserNeed(userId, userNeedId);
        userNeed.softDelete();
    }

    public UserNeedListResponseDto getUserNeeds(Long userId) {
        List<UserNeed> userNeeds = userNeedRepository.findByUser_IdAndDeletedAtIsNullOrderByIdDesc(userId);
        if (userNeeds.isEmpty()) {
            return new UserNeedListResponseDto(List.of());
        }

        List<Long> userNeedIds = userNeeds.stream()
                .map(UserNeed::getId)
                .toList();

        Map<Long, List<String>> houseTypesByNeedId = getHouseTypesByNeedIds(userNeedIds);
        Map<Long, List<String>> lifestyleTagsByNeedId = getLifestyleTagsByNeedIds(userNeedIds);
        Map<Long, List<String>> floorsByNeedId = getFloorsByNeedIds(userNeedIds);

        List<UserNeedListResponseDto.UserNeedConditionDto> conditions = userNeeds.stream()
                .map(userNeed -> {
                    return new UserNeedListResponseDto.UserNeedConditionDto(
                            userNeed.getId(),
                            resolveConditionName(userNeed),
                            userNeed.getIsDefaultNeed(),
                            new UserNeedListResponseDto.UserNeedSummaryDto(
                                    userNeed.getSidoName(),
                                    userNeed.getSigunguName(),
                                    userNeed.getTargetAddress(),
                                    toDisplayRentType(userNeed.getRentType()),
                                    userNeed.getMaxDeposit(),
                                    userNeed.getMaxMonthlyRent(),
                                    getOrEmpty(houseTypesByNeedId, userNeed.getId()),
                                    getOrEmpty(lifestyleTagsByNeedId, userNeed.getId()),
                                    getOrEmpty(floorsByNeedId, userNeed.getId())
                            )
                    );
                })
                .toList();

        return new UserNeedListResponseDto(conditions);
    }

    public UserNeedDetailResponseDto getUserNeedDetail(Long userId, Long userNeedId) {
        UserNeed userNeed = getActiveUserNeed(userId, userNeedId);
        return buildUserNeedDetailResponse(userNeed);
    }

    @Transactional
    public UserNeedDefaultResponseDto setUserNeedDefault(Long userId, Long userNeedId) {
        UserNeed targetUserNeed = getActiveUserNeed(userId, userNeedId);

        List<UserNeed> userNeeds = userNeedRepository.findByUser_IdAndDeletedAtIsNullOrderByIdDesc(userId);
        for (UserNeed userNeed : userNeeds) {
            userNeed.setDefaultNeed(Boolean.FALSE);
        }

        targetUserNeed.setDefaultNeed(Boolean.TRUE);

        return new UserNeedDefaultResponseDto(userNeedId, targetUserNeed.getIsDefaultNeed());
    }

    private String normalizeNeedName(String needName) {
        if (needName == null || needName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "조건 이름은 필수입니다.");
        }
        return needName.trim();
    }

    private String normalizeRegionName(String regionName, String fieldName) {
        if (regionName == null || regionName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, fieldName + "은 필수입니다.");
        }
        return regionName.trim();
    }

    private org.locationtech.jts.geom.Point createTargetPoint(UserNeedRequestDto request) {
        return geometryFactory.createPoint(new Coordinate(request.getLng(), request.getLat()));
    }

    private String resolveConditionName(UserNeed userNeed) {
        if (userNeed.getNeedName() == null || userNeed.getNeedName().isBlank()) {
            return "주거조건 " + userNeed.getId();
        }
        return userNeed.getNeedName().trim();
    }

    private UserNeedDetailResponseDto buildUserNeedDetailResponse(UserNeed userNeed) {
        List<String> housingTypes = getOrEmpty(getHouseTypesByNeedIds(List.of(userNeed.getId())), userNeed.getId());
        List<String> lifestyleTags = getOrEmpty(getLifestyleTagsByNeedIds(List.of(userNeed.getId())), userNeed.getId());
        List<String> floors = getOrEmpty(getFloorsByNeedIds(List.of(userNeed.getId())), userNeed.getId());

        return new UserNeedDetailResponseDto(
                resolveConditionName(userNeed),
                userNeed.getTargetAddress(),
                userNeed.getSidoName(),
                userNeed.getSigunguName(),
                userNeed.getTargetPos() == null ? null : userNeed.getTargetPos().getY(),
                userNeed.getTargetPos() == null ? null : userNeed.getTargetPos().getX(),
                toDisplayRentType(userNeed.getRentType()),
                userNeed.getMinDeposit(),
                userNeed.getMaxDeposit(),
                userNeed.getMinMonthlyRent(),
                userNeed.getMaxMonthlyRent(),
                userNeed.getMaxCommuteTime(),
                parseFloorSize(userNeed.getMinExclusiveSize()),
                housingTypes,
                lifestyleTags,
                floors
        );
    }

    private void validateRequest(UserNeedRequestDto request) {
        if (request.getMaxDeposit() != null && request.getMaxDeposit() < request.getMinDeposit()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "최대 보증금이 최소 보증금보다 작을 수 없습니다.");
        }
        if (request.getMaxMonthlyRent() != null && request.getMaxMonthlyRent() < request.getMinMonthlyRent()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "최대 월세가 최소 월세보다 작을 수 없습니다.");
        }
    }

    private void saveHouseTypes(UserNeed userNeed, List<String> housingTypes) {
        List<CommonCodeDetail> codeDetails = resolveCommonCodeDetails(HOUSE_TYPE_GROUP_ID, housingTypes, "주택 유형");
        if (codeDetails.isEmpty()) {
            return;
        }

        List<UserNeedHouseType> entities = codeDetails.stream()
                .map(codeDetail -> UserNeedHouseType.builder()
                        .id(new UserNeedHouseTypeId(userNeed.getId(), codeDetail.getId()))
                        .userNeed(userNeed)
                        .commonCodeDetail(codeDetail)
                        .build())
                .toList();
        userNeedHouseTypeRepository.saveAll(entities);
    }

    private void replaceHouseTypes(UserNeed userNeed, List<String> housingTypes) {
        userNeedHouseTypeRepository.deleteByIdUserNeedId(userNeed.getId());
        saveHouseTypes(userNeed, housingTypes);
    }

    private void saveLifestyleTags(UserNeed userNeed, List<String> lifestyleTags) {
        List<CommonCodeDetail> codeDetails = resolveCommonCodeDetails(LIFESTYLE_TAG_GROUP_ID, lifestyleTags, "생활 태그");
        if (codeDetails.isEmpty()) {
            return;
        }

        List<UserNeedLifestyleTag> entities = codeDetails.stream()
                .map(codeDetail -> UserNeedLifestyleTag.builder()
                        .id(new UserNeedLifestyleTagId(userNeed.getId(), codeDetail.getId()))
                        .userNeed(userNeed)
                        .commonCodeDetail(codeDetail)
                        .build())
                .toList();
        userNeedLifestyleTagRepository.saveAll(entities);
    }

    private void replaceLifestyleTags(UserNeed userNeed, List<String> lifestyleTags) {
        userNeedLifestyleTagRepository.deleteByIdUserNeedId(userNeed.getId());
        saveLifestyleTags(userNeed, lifestyleTags);
    }

    private void saveFloors(UserNeed userNeed, List<String> floors) {
        List<CommonCodeDetail> codeDetails = resolveCommonCodeDetails(FLOOR_GROUP_ID, floors, "층 구분");
        if (codeDetails.isEmpty()) {
            return;
        }

        List<UserNeedFloor> entities = codeDetails.stream()
                .map(codeDetail -> UserNeedFloor.builder()
                        .id(new UserNeedFloorId(userNeed.getId(), codeDetail.getId()))
                        .userNeed(userNeed)
                        .commonCodeDetail(codeDetail)
                        .build())
                .toList();
        userNeedFloorRepository.saveAll(entities);
    }

    private void replaceFloors(UserNeed userNeed, List<String> floors) {
        userNeedFloorRepository.deleteByIdUserNeedId(userNeed.getId());
        saveFloors(userNeed, floors);
    }

    private List<String> normalizeValues(List<String> values, String label) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, label + " 값에 빈 문자열이 포함될 수 없습니다.");
            }
            normalized.add(value.trim());
        }
        return List.copyOf(normalized);
    }

    private List<CommonCodeDetail> resolveCommonCodeDetails(Long groupId, List<String> values, String label) {
        List<String> normalizedValues = normalizeValues(values, label);
        if (normalizedValues.isEmpty()) {
            return Collections.emptyList();
        }

        List<CommonCodeDetail> codeDetails =
                commonCodeDetailRepository.findByGroup_IdAndCodeNameInAndIsActiveTrue(groupId, normalizedValues);
        Map<String, CommonCodeDetail> detailsByName = new HashMap<>();
        for (CommonCodeDetail codeDetail : codeDetails) {
            detailsByName.put(codeDetail.getCodeName(), codeDetail);
        }

        List<String> invalidValues = normalizedValues.stream()
                .filter(value -> !detailsByName.containsKey(value))
                .toList();
        if (!invalidValues.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    label + " 공통코드가 유효하지 않습니다: " + String.join(", ", invalidValues)
            );
        }

        return normalizedValues.stream()
                .map(detailsByName::get)
                .toList();
    }

    private Map<Long, List<String>> getHouseTypesByNeedIds(Collection<Long> userNeedIds) {
        if (userNeedIds == null || userNeedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, List<String>> result = new HashMap<>();
        for (UserNeedHouseType houseType :
                userNeedHouseTypeRepository.findByIdUserNeedIdInOrderByCommonCodeDetailSortOrderAsc(userNeedIds)) {
            result.computeIfAbsent(houseType.getId().getUserNeedId(), key -> new ArrayList<>())
                    .add(houseType.getCommonCodeDetail().getCodeName());
        }
        return result;
    }

    private Map<Long, List<String>> getFloorsByNeedIds(Collection<Long> userNeedIds) {
        if (userNeedIds == null || userNeedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, List<String>> result = new HashMap<>();
        for (UserNeedFloor floor :
                userNeedFloorRepository.findByIdUserNeedIdInOrderByCommonCodeDetailSortOrderAsc(userNeedIds)) {
            result.computeIfAbsent(floor.getId().getUserNeedId(), key -> new ArrayList<>())
                    .add(floor.getCommonCodeDetail().getCodeName());
        }
        return result;
    }

    private Map<Long, List<String>> getLifestyleTagsByNeedIds(Collection<Long> userNeedIds) {
        if (userNeedIds == null || userNeedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, List<String>> result = new HashMap<>();
        for (UserNeedLifestyleTag lifestyleTag :
                userNeedLifestyleTagRepository.findByIdUserNeedIdInOrderByCommonCodeDetailSortOrderAsc(userNeedIds)) {
            result.computeIfAbsent(lifestyleTag.getId().getUserNeedId(), key -> new ArrayList<>())
                    .add(lifestyleTag.getCommonCodeDetail().getCodeName());
        }
        return result;
    }

    private List<String> getOrEmpty(Map<Long, List<String>> valuesByNeedId, Long userNeedId) {
        return valuesByNeedId.getOrDefault(userNeedId, List.of());
    }

    private String normalizeRentType(String rentType) {
        return switch (rentType.trim().toUpperCase()) {
            case "JEONSE", "전세" -> "JEONSE";
            case "MONTHLY", "월세" -> "MONTHLY";
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT, "rentType은 JEONSE 또는 MONTHLY여야 합니다.");
        };
    }

    private String toDisplayRentType(String rentType) {
        return switch (rentType) {
            case "JEONSE" -> "전세";
            case "MONTHLY" -> "월세";
            default -> rentType;
        };
    }

    private Double parseFloorSize(String floorSize) {
        if (floorSize == null || floorSize.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(floorSize);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "저장된 면적 값을 숫자로 변환할 수 없습니다.");
        }
    }

    private UserNeed getActiveUserNeed(Long userId, Long userNeedId) {
        return userNeedRepository.findByIdAndUser_IdAndDeletedAtIsNull(userNeedId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 ID의 주거 조건을 찾을 수 없습니다."));
    }

}
