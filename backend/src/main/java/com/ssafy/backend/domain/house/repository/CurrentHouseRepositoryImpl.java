package com.ssafy.backend.domain.house.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.backend.domain.code.service.CommonCodeService;
import com.ssafy.backend.domain.house.dto.request.MapSearchRequest;
import com.ssafy.backend.domain.house.entity.CurrentHouse;
import com.ssafy.backend.domain.house.entity.QCurrentHouse;
import com.ssafy.backend.domain.region.entity.RegionSido;
import com.ssafy.backend.domain.region.entity.RegionSigungu;
import com.ssafy.backend.domain.region.repository.RegionSidoRepository;
import com.ssafy.backend.domain.region.repository.RegionSigunguRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class CurrentHouseRepositoryImpl implements CurrentHouseRepositoryQuerydsl {
    private final JPAQueryFactory queryFactory;
    private final RegionSidoRepository regionSidoRepository;
    private final RegionSigunguRepository regionSigunguRepository;
    private final CommonCodeService commonCodeService;

    @Override
    public List<CurrentHouse> findAllByFilterCondition(MapSearchRequest req) {
        QCurrentHouse house = QCurrentHouse.currentHouse;
        BooleanBuilder builder = new BooleanBuilder();

        // 매물 상태가 10인 결과만 조회
        builder.and(house.houseStatusCode.eq(commonCodeService.getCommonCode("거래가능")));

        String sidoCode = null;
        // 1. 시도 코드 (sidoName -> sidoCode)
        if (StringUtils.hasText(req.getSidoName())) {
            RegionSido regionSido = regionSidoRepository.findBySidoName(req.getSidoName()).orElse(null);
            if (regionSido != null) {
                builder.and(house.sidoCode.eq(regionSido.getSidoCode()));
                sidoCode = regionSido.getSidoCode();
            } else {
                // 시도가 없는 경우 결과 없음
                return List.of();
            }
        }

        // 2. 시군구 코드 (sidoName + sigunguName -> sigunguCode)
        if (StringUtils.hasText(req.getSigunguName()) && sidoCode != null) {
            RegionSigungu regionSigungu = regionSigunguRepository.findByRegionSidoSidoCodeAndSigunguName(sidoCode, req.getSigunguName())
                    .orElse(null);
            if (regionSigungu != null) {
                builder.and(house.sigunguCode.eq(regionSigungu.getSigunguCode()));
            } else {
                // 시군구가 없는 경우 결과 없음
                return List.of();
            }
        }

        // 3. 주거유형 (houseType names -> houseTypeCode list)
        if (!CollectionUtils.isEmpty(req.getHouseType())) {
            List<Long> houseTypeCodes = commonCodeService.getCommonCodeList(req.getHouseType());
            if (!houseTypeCodes.isEmpty()) {
                builder.and(house.houseTypeCode.in(houseTypeCodes));
            } else {
                // 해당 이름의 코드가 없으면 결과 없음 (엄격 모드라면)
                // 하지만 필터 조건에 맞는 코드가 DB에 없으면 해당 조건으로 검색 시 결과 0건이 맞음
                return List.of();
            }
        }

        // 4. 계약형태 (rentType names -> rentTypeCode list)
        if (!CollectionUtils.isEmpty(req.getRentType())) {
            List<Long> rentTypeCodes = commonCodeService.getCommonCodeList(req.getRentType());
            if (!rentTypeCodes.isEmpty()) {
                builder.and(house.rentTypeCode.in(rentTypeCodes));
            } else {
                return List.of();
            }
        }

        // 5. 월세 범위
        if (req.getMinMonthlyCost() != null && req.getMinMonthlyCost() >= 0) {
            builder.and(house.monthlyCost.goe(req.getMinMonthlyCost()));
        }
        if (req.getMaxMonthlyCost() != null && req.getMaxMonthlyCost() >= 0) {
            builder.and(house.monthlyCost.loe(req.getMaxMonthlyCost()));
        }

        // 6. 보증금 범위
        if (req.getMinDeposit() != null && req.getMinDeposit() >= 0) {
            builder.and(house.deposit.goe(req.getMinDeposit()));
        }
        if (req.getMaxDeposit() != null && req.getMaxDeposit() >= 0) {
            builder.and(house.deposit.loe(req.getMaxDeposit()));
        }

        // 7. 관리비 (입력값 이하)
        if (req.getManagementCost() != null && req.getManagementCost() > 0) {
            builder.and(house.managementCost.loe(req.getManagementCost()));
        }

        // 8. 연면적 범위
        if (req.getMinFloorSize() != null && req.getMinFloorSize() >= 0) {
            builder.and(house.floorSize.goe(req.getMinFloorSize()));
        }
        if (req.getMaxFloorSize() != null && req.getMaxFloorSize() >= 0) {
            builder.and(house.floorSize.loe(req.getMaxFloorSize()));
        }

        // 9. 층 수 (floor names -> floorCode list)
        if (!CollectionUtils.isEmpty(req.getFloor())) {
            List<Long> floorCodes = commonCodeService.getCommonCodeList(req.getFloor());
            if (!floorCodes.isEmpty()) {
                builder.and(house.floorCode.in(floorCodes));
            } else {
                return List.of();
            }
        }

        if (req.getLastId() != null && req.getLastId() > 0 && req.getPageSize() != null) {
            builder.and(house.id.lt(req.getLastId()));
        }

        var query = queryFactory
                .selectFrom(house)
                .where(builder);

        if (req.getLastId() != null && req.getPageSize() != null) {
            query.orderBy(house.id.desc())
                    .limit(req.getPageSize() + 1);
        }

        return query.fetch();
    }
}
