package com.ssafy.backend.domain.house.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.batch.entity.OutboxStatus;
import com.ssafy.backend.batch.entity.VectorOutbox;
import com.ssafy.backend.batch.repository.OutboxRepository;
import com.ssafy.backend.domain.code.service.CommonCodeService;
import com.ssafy.backend.domain.house.dto.request.CompleteRequest;
import com.ssafy.backend.domain.house.dto.request.CurrentHouseRequest;
import com.ssafy.backend.domain.house.dto.request.HousePageable;
import com.ssafy.backend.domain.house.dto.request.MapSearchRequest;
import com.ssafy.backend.domain.house.dto.response.*;
import com.ssafy.backend.domain.house.entity.CurrentHouse;
import com.ssafy.backend.domain.house.entity.HouseImage;
import com.ssafy.backend.domain.house.entity.Wishlist;
import com.ssafy.backend.domain.house.entity.WishlistId;
import com.ssafy.backend.domain.house.repository.CurrentHouseRepository;
import com.ssafy.backend.domain.house.repository.HouseImageRepository;
import com.ssafy.backend.domain.house.repository.WishListRepository;
import com.ssafy.backend.domain.infrastructure.dto.response.NearbyInfrastructureResponse;
import com.ssafy.backend.domain.infrastructure.service.InfrastructureService;
import com.ssafy.backend.domain.infrastructure.type.InfrastructureType;
import com.ssafy.backend.domain.region.service.RegionService;
import com.ssafy.backend.domain.user.entity.User;
import com.ssafy.backend.domain.user.repository.UserRepository;
import com.ssafy.backend.global.cacheable.CacheStrategy;
import com.ssafy.backend.global.cacheable.MyCachePut;
import com.ssafy.backend.global.cacheable.MyCacheable;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrentHouseService {
    private final CurrentHouseRepository currentHouseRepository;
    private final WishListRepository wishListRepository;
    private final UserRepository userRepository;
    private final OutboxRepository outboxRepository;
    private final HouseImageRepository houseImageRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private final RegionService regionService;
    private final CommonCodeService commonCodeService;
    private final KakaoGeoService kakaoGeoService;
    private final InfrastructureService infrastructureService;

    private final ObjectMapper objectMapper;

    @Value("${aws.cloudfront.url}")
    private String urlPrefix;

    public AddressValidResponse validateAddress(String address) {
        kakaoGeoService.getCoordinates(address);
        return new AddressValidResponse(true, "유효한 주소입니다.");
    }

    @Transactional
    public HouseCreateResponse create(CurrentHouseRequest req, Long userId) {
        KakaoAddressResponse.Document response = kakaoGeoService.getCoordinates(req.getAddress());

        Point position = geometryFactory.createPoint(new Coordinate(
                Double.parseDouble(response.x()),
                Double.parseDouble(response.y())
        ));

        CurrentHouse currentHouse = CurrentHouse.from(
                req,
                userId,
                regionService.getRegionCode(req.getSidoName(), req.getSigunguName(), req.getDongName()),
                regionService.getSidoCode(req.getSidoName()),
                regionService.getSigunguCode(req.getSidoName(), req.getSigunguName()),
                commonCodeService.getCommonCode(req.getHouseType()),
                commonCodeService.getCommonCode(req.getRentType()),
                commonCodeService.getCommonCode("거래가능"),
                commonCodeService.getCommonCode(req.getFloor()),
                position
        );
        CurrentHouse saved = currentHouseRepository.save(currentHouse);

        if (req.getImages() != null && !req.getImages().isEmpty()) {
            List<HouseImage> images = req.getImages().stream()
                    .map(imgReq -> {
                        HouseImage image = new HouseImage();
                        image.setCurrentHouse(saved);
                        image.setImageUrl(urlPrefix + "/" + imgReq.objectKey());
                        image.setIsThumbnail(imgReq.isThumbnail() != null && imgReq.isThumbnail());
                        return image;
                    })
                    .toList();
            houseImageRepository.saveAll(images);
        }

        Map<String, Object> metadata = new HashMap<>();

        metadata.put("sidoName", req.getSidoName());
        metadata.put("sigunguName", req.getSigunguName());
        metadata.put("houseType", req.getHouseType());
        metadata.put("rentType", req.getRentType());
        metadata.put("monthlyCost", req.getMonthlyCost());
        metadata.put("deposit", req.getDeposit());
        metadata.put("floorSize", req.getFloorSize());
        metadata.put("floor", req.getFloor());

        Map<String, Object> contentMap = objectMapper.convertValue(
                req,
                new TypeReference<Map<String, Object>>() {
                }
        );
        // 2. VectorOutbox 객체 빌드 (동일 트랜잭션 내 처리)
        // savedHouse -> saved 로 수정 (위에서 선언한 변수명 사용)
        VectorOutbox outboxEvent = VectorOutbox.builder()
                .houseId(saved.getId())
                .content(contentMap)
                .metadata(metadata)
                .status(OutboxStatus.READY)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        // 3. 저장 (여기서 예외 발생 시 CurrentHouse 저장도 같이 롤백됨 -> 데이터 정합성 보장)
        outboxRepository.save(outboxEvent);


        return new HouseCreateResponse(saved.getId(), saved.getCreatedAt());
    }

    @MyCacheable(
            cacheName = "currentHouse",
            key = "#houseId",
            ttlSeconds = 3600, // 1시간
            cacheStrategy = CacheStrategy.LOOK_ASIDE
    )
    @Transactional(readOnly = true)
    public CurrentHouseResponse findById(Long houseId) {
        CurrentHouse house = currentHouseRepository.findById(houseId)
                .orElseThrow(() -> new EntityNotFoundException("CurrentHouse not found id=" + houseId));
        return CurrentHouseResponse.from(
                house,
                regionService.getSidoName(house.getSidoCode()),
                regionService.getSigunguName(house.getSigunguCode()),
                regionService.getRegionName(house.getRegionCode()),
                commonCodeService.getCommonCodeName(house.getHouseTypeCode()),
                commonCodeService.getCommonCodeName(house.getRentTypeCode()),
                commonCodeService.getCommonCodeName(house.getHouseStatusCode()),
                commonCodeService.getCommonCodeName(house.getFloorCode())
        );
    }

    @Transactional(readOnly = true)
    public CurrentHouseSearchResponse findByIdWithStats(Long houseId) {
        CurrentHouse house = currentHouseRepository.findById(houseId)
                .orElseThrow(() -> new EntityNotFoundException("CurrentHouse not found id=" + houseId));

        CurrentHouseSearchResponse response = CurrentHouseSearchResponse.from(
                house,
                regionService.getSidoName(house.getSidoCode()),
                regionService.getSigunguName(house.getSigunguCode()),
                regionService.getRegionName(house.getRegionCode()),
                commonCodeService.getCommonCodeName(house.getHouseTypeCode()),
                commonCodeService.getCommonCodeName(house.getRentTypeCode()),
                commonCodeService.getCommonCodeName(house.getHouseStatusCode()),
                commonCodeService.getCommonCodeName(house.getFloorCode())
        );

        NearbyInfrastructureResponse nearby = infrastructureService.getNearbyInfrastructures(houseId);
        Map<String, NearbyInfrastructureResponse.Summary> summaryByType = nearby.getSummaries().stream()
                .collect(Collectors.toMap(
                        NearbyInfrastructureResponse.Summary::getType,
                        Function.identity(),
                        (left, right) -> left
                ));

        response.setInfraCount(new CurrentHouseSearchResponse.InfraCount(
                getCount(summaryByType, InfrastructureType.CONVENIENCE),
                getCount(summaryByType, InfrastructureType.LAUNDRY),
                getCount(summaryByType, InfrastructureType.CAFE),
                getCount(summaryByType, InfrastructureType.HOSPITAL),
                getCount(summaryByType, InfrastructureType.PHARMACY),
                getCount(summaryByType, InfrastructureType.BUS)
        ));

        response.setMinDist(new CurrentHouseSearchResponse.MinDist(
                getNearestDistance(summaryByType, InfrastructureType.CONVENIENCE),
                getNearestDistance(summaryByType, InfrastructureType.LAUNDRY),
                getNearestDistance(summaryByType, InfrastructureType.CAFE),
                getNearestDistance(summaryByType, InfrastructureType.HOSPITAL),
                getNearestDistance(summaryByType, InfrastructureType.PHARMACY),
                getNearestDistance(summaryByType, InfrastructureType.SUBWAY)
        ));

        return response;
    }

    private Integer getCount(Map<String, NearbyInfrastructureResponse.Summary> summaryByType, InfrastructureType type) {
        NearbyInfrastructureResponse.Summary summary = summaryByType.get(type.getCode());
        return summary == null || summary.getCount() == null ? 0 : summary.getCount();
    }

    private Integer getNearestDistance(Map<String, NearbyInfrastructureResponse.Summary> summaryByType, InfrastructureType type) {
        NearbyInfrastructureResponse.Summary summary = summaryByType.get(type.getCode());
        return summary == null ? null : summary.getNearestDistanceMeters();
    }

    @Transactional(readOnly = true)
    public CurrentHouseListResponse findAll(Long userId, com.ssafy.backend.domain.house.dto.request.HousePageable pageable) {
        Page<CurrentHouse> pageResult;
        if (pageable.getPage() == null || pageable.getSize() == null) {
            pageResult = currentHouseRepository.findAllByUserId(userId, Pageable.unpaged());
        } else {
            PageRequest p = PageRequest.of(pageable.getPage(), pageable.getSize());
            pageResult = currentHouseRepository.findAllByUserId(userId, p);
        }

        return new CurrentHouseListResponse(
                pageResult.stream()
                        .map(ele -> CurrentHouseListResponse.ListElement.from(
                                ele,
                                regionService.getSidoName(ele.getSidoCode()),
                                regionService.getSigunguName(ele.getSigunguCode()),
                                regionService.getRegionName(ele.getRegionCode()),
                                commonCodeService.getCommonCodeName(ele.getHouseTypeCode()),
                                commonCodeService.getCommonCodeName(ele.getRentTypeCode()),
                                commonCodeService.getCommonCodeName(ele.getHouseStatusCode()),
                                ele.getFloorCode() == null
                                        ? null
                                        : commonCodeService.getCommonCodeName(ele.getFloorCode())))
                        .toList(),
                pageResult.getTotalPages());
    }

    @Transactional(readOnly = true)
    public CurrentHouseListResponse findWishList(Long userId, HousePageable pageable) {
        Page<CurrentHouse> pageResult;
        if (pageable.getPage() == null || pageable.getSize() == null) {
            pageResult = wishListRepository.findWishListHousesByUserIdWithPaging(userId, Pageable.unpaged());
        } else {
            PageRequest p = PageRequest.of(pageable.getPage(), pageable.getSize());
            pageResult = wishListRepository.findWishListHousesByUserIdWithPaging(userId, p);
        }

        return new CurrentHouseListResponse(
                pageResult.stream()
                        .map(ele -> CurrentHouseListResponse.ListElement.from(
                                ele,
                                regionService.getSidoName(ele.getSidoCode()),
                                regionService.getSigunguName(ele.getSigunguCode()),
                                regionService.getRegionName(ele.getRegionCode()),
                                commonCodeService.getCommonCodeName(ele.getHouseTypeCode()),
                                commonCodeService.getCommonCodeName(ele.getRentTypeCode()),
                                commonCodeService.getCommonCodeName(ele.getHouseStatusCode()),
                                ele.getFloorCode() == null
                                        ? null
                                        : commonCodeService.getCommonCodeName(ele.getFloorCode())))
                        .toList(),
                pageResult.getTotalPages());
    }

    @Transactional(readOnly = true)
    public CurrentHouseListScrollResponse findAllByFilterCondition(MapSearchRequest req) {
        List<CurrentHouse> houses = currentHouseRepository.findAllByFilterCondition(req);

        boolean hasNext = false;
        Long nextLastId = null;

        if (req.getPageSize() != null && houses.size() > req.getPageSize()) {
            hasNext = true;
            houses = houses.subList(0, req.getPageSize().intValue());
        }

        if (!houses.isEmpty() && req.getPageSize() != null) {
            nextLastId = houses.get(houses.size() - 1).getId();
        }

        return new CurrentHouseListScrollResponse(
                houses.stream()
                        .map(ele -> CurrentHouseListResponse.ListElement.from(
                                ele,
                                regionService.getSidoName(ele.getSidoCode()),
                                regionService.getSigunguName(ele.getSigunguCode()),
                                regionService.getRegionName(ele.getRegionCode()),
                                commonCodeService.getCommonCodeName(ele.getHouseTypeCode()),
                                commonCodeService.getCommonCodeName(ele.getRentTypeCode()),
                                commonCodeService.getCommonCodeName(ele.getHouseStatusCode()),
                                ele.getFloorCode() == null
                                        ? null
                                        : commonCodeService.getCommonCodeName(ele.getFloorCode())))
                        .toList(),
                hasNext,
                nextLastId
        );
    }

    @Transactional
    public boolean toggleWishList(Long houseId, Long userId) {
        WishlistId wishlistId = new WishlistId(userId, houseId);

        return wishListRepository.findById(wishlistId)
                .map(wishlist -> {
                    // 이미 존재하면 삭제
                    wishListRepository.delete(wishlist);
                    return false;
                })
                .orElseGet(() -> {
                    // 존재하지 않으면 추가 (프록시 객체 활용하여 Select 쿼리 최소화)
                    User user = userRepository.getReferenceById(userId);
                    CurrentHouse house = currentHouseRepository.getReferenceById(houseId);

                    Wishlist wishlist = new Wishlist(userId, houseId, user, house);
                    wishListRepository.save(wishlist);
                    return true;
                });
    }

    @MyCachePut(
            cacheName = "currentHouse",
            key = "#houseId",
            ttlSeconds = 3600, // 1시간
            cacheStrategy = CacheStrategy.LOOK_ASIDE
    )
    @Transactional
    public CurrentHouseResponse update(Long houseId, CurrentHouseRequest req) {
        if (houseId == null) {
            throw new BusinessException(ErrorCode.FAIL, "House ID must not be null for update.");
        }

        CurrentHouse house = currentHouseRepository.findById(houseId)
                .orElseThrow(() -> new EntityNotFoundException("CurrentHouse not found id=" + req.getHouseId()));

        if (req.getRentType() != null) {
            house.setRentTypeCode(commonCodeService.getCommonCode(req.getRentType()));
        }
        if (req.getDeposit() != null) {
            house.setDeposit(req.getDeposit());
        }
        if (req.getMonthlyCost() != null) {
            house.setMonthlyCost(req.getMonthlyCost());
        }
        if (req.getManagementCost() != null) {
            house.setManagementCost(req.getManagementCost());
        }
        if (req.getManagementItems() != null) {
            house.setManagementItems(req.getManagementItems());
        }
        if (req.getContractStartYearMonth() != null) {
            house.setContractStart(req.getContractStartYearMonth());
        }
        if (req.getContractEndYearMonth() != null) {
            house.setContractEnd(req.getContractEndYearMonth());
        }
        if (req.getDescription() != null) {
            house.setDescription(req.getDescription());
        }

        outboxRepository.findByHouseId(houseId).ifPresent(outbox -> {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sidoName", regionService.getSidoName(house.getSidoCode()));
            metadata.put("sigunguName", regionService.getSigunguName(house.getSigunguCode()));
            metadata.put("houseType", commonCodeService.getCommonCodeName(house.getHouseTypeCode()));
            metadata.put("rentType", commonCodeService.getCommonCodeName(house.getRentTypeCode()));
            metadata.put("monthlyCost", house.getMonthlyCost());
            metadata.put("deposit", house.getDeposit());
            metadata.put("floorSize", house.getFloorSize());
            metadata.put("floor", commonCodeService.getCommonCodeName(house.getFloorCode()));

            Map<String, Object> contentMap = objectMapper.convertValue(req, new TypeReference<>() {
            });
            outbox.updateData(contentMap, metadata);
            outboxRepository.save(outbox);
        });

        return CurrentHouseResponse.from(
                house,
                regionService.getSidoName(house.getSidoCode()),
                regionService.getSigunguName(house.getSigunguCode()),
                regionService.getRegionName(house.getRegionCode()),
                commonCodeService.getCommonCodeName(house.getHouseTypeCode()),
                commonCodeService.getCommonCodeName(house.getRentTypeCode()),
                commonCodeService.getCommonCodeName(house.getHouseStatusCode()),
                commonCodeService.getCommonCodeName(house.getFloorCode())
        );
    }

    @Transactional
    public void addHouseImages(List<CompleteRequest> req) {
        if (req == null || req.isEmpty()) {
            throw new BusinessException(ErrorCode.FAIL, "Request list must not be null or empty.");
        }

        CurrentHouse house = currentHouseRepository.findById(req.get(0).houseId())
                .orElseThrow(() -> new EntityNotFoundException("CurrentHouse not found id=" + req.get(0).houseId()));

        List<HouseImage> images = req.stream()
                .map(r -> {
                    HouseImage image = new HouseImage();
                    image.setCurrentHouse(house);
                    image.setImageUrl(r.objectKey());
                    image.setIsThumbnail(r.isThumbnail() != null && r.isThumbnail());
                    return image;
                })
                .toList();

        houseImageRepository.saveAll(images);
    }


    @MyCachePut( // Soft delete라서 남김
            cacheName = "currentHouse",
            key = "#houseId",
            ttlSeconds = 3600, // 1시간
            cacheStrategy = CacheStrategy.LOOK_ASIDE
    )
    @Transactional
    public CurrentHouseResponse delete(Long houseId, Long userId) {
        CurrentHouse house = currentHouseRepository.findById(houseId)
                .orElseThrow(() -> new EntityNotFoundException("CurrentHouse not found id=" + houseId));

//        if (!house.getUserId().equals(userId)) {
//            throw new BusinessException(ErrorCode.FAIL, "해당 매물을 삭제할 권한이 없습니다.");
//        }

        house.setHouseStatusCode(commonCodeService.getCommonCode("매물삭제"));
        LocalDateTime deletedAt = LocalDateTime.now();
        house.setDeletedAt(deletedAt);

        wishListRepository.deleteByHouseId(houseId);

        outboxRepository.findByHouseId(houseId).ifPresent(ele -> {
            ele.markReadyDelete();
            outboxRepository.save(ele);
        });

        return CurrentHouseResponse.from(
                house,
                regionService.getSidoName(house.getSidoCode()),
                regionService.getSigunguName(house.getSigunguCode()),
                regionService.getRegionName(house.getRegionCode()),
                commonCodeService.getCommonCodeName(house.getHouseTypeCode()),
                commonCodeService.getCommonCodeName(house.getRentTypeCode()),
                commonCodeService.getCommonCodeName(house.getHouseStatusCode()),
                commonCodeService.getCommonCodeName(house.getFloorCode())
        );
    }

    @MyCachePut(
            cacheName = "currentHouse",
            key = "#houseId",
            ttlSeconds = 3600, // 1시간
            cacheStrategy = CacheStrategy.LOOK_ASIDE
    )
    @Transactional
    public CurrentHouseResponse sold(Long houseId) {
        CurrentHouse house = currentHouseRepository.findById(houseId)
                .orElseThrow(() -> new EntityNotFoundException("CurrentHouse not found id=" + houseId));
        house.setHouseStatusCode(commonCodeService.getCommonCode("거래완료"));
        house.setSoldAt(LocalDateTime.now());

        wishListRepository.deleteByHouseId(houseId);

        outboxRepository.findByHouseId(houseId).ifPresent(ele -> {
            ele.markReadyDelete();
            outboxRepository.save(ele);
        });

        return CurrentHouseResponse.from(
                house,
                regionService.getSidoName(house.getSidoCode()),
                regionService.getSigunguName(house.getSigunguCode()),
                regionService.getRegionName(house.getRegionCode()),
                commonCodeService.getCommonCodeName(house.getHouseTypeCode()),
                commonCodeService.getCommonCodeName(house.getRentTypeCode()),
                commonCodeService.getCommonCodeName(house.getHouseStatusCode()),
                commonCodeService.getCommonCodeName(house.getFloorCode())
        );
    }

    @Transactional(readOnly = true)
    public Boolean checkLiked(Long houseId, Long userId) {
        return wishListRepository.existsByUserIdAndHouseId(userId, houseId);
    }

    @Transactional(readOnly = true)
    public Map<Long, Boolean> checkLikedList(Long userId, List<Long> houseIds) {
        if (houseIds == null || houseIds.isEmpty()) {
            return new HashMap<>();
        }
        Set<Long> likedHouseIds = wishListRepository.findLikedHouseIdsByUserIdAndHouseIds(userId, houseIds);
        return houseIds.stream()
                .collect(Collectors.toMap(
                        houseId -> houseId,
                        likedHouseIds::contains,
                        (existing, replacement) -> existing
                ));
    }
}
