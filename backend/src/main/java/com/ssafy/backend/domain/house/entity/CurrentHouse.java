package com.ssafy.backend.domain.house.entity;

import com.ssafy.backend.domain.house.dto.request.CurrentHouseRequest;
import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "current_house",
        indexes = {
                @Index(name = "idx_created_by", columnList = "created_by"),
        }
)
@Getter
@Setter
public class CurrentHouse extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "region_code", length = 10, nullable = false)
    private String regionCode;

    @Column(name = "sido_code", length = 2, nullable = false)
    private String sidoCode;

    @Column(name = "sigungu_code", length = 5, nullable = false)
    private String sigunguCode;

    @Column(name = "house_type_code", nullable = false)
    private Long houseTypeCode;

    @Column(name = "rent_type_code", nullable = false)
    private Long rentTypeCode;

    @Column(name = "house_status_code", nullable = false)
    private Long houseStatusCode;

    @Column(name = "floor_code", nullable = false)
    private Long floorCode;

    @Column(name = "address", nullable = false, length = 256)
    private String address;

    @Column(name = "position", nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point position;

    @Column(name = "deposit", nullable = false)
    private Integer deposit;

    @Column(name = "monthly_cost", nullable = false)
    private Integer monthlyCost;

    @Column(name = "management_cost")
    private Integer managementCost = 0;

    @Column(name = "management_items", length = 256)
    private String managementItems;

    @Column(name = "floor_size", nullable = false)
    private Double floorSize;

    @Column(name = "contract_start", nullable = false)
    private Integer contractStart;

    @Column(name = "contract_end", nullable = false)
    private Integer contractEnd;

    @Column(name = "build_year")
    private Integer buildYear;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description = "";

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 양방향 연관관계
    @OneToMany(mappedBy = "currentHouse")
    private List<HouseImage> images = new ArrayList<>();

    public static CurrentHouse from(CurrentHouseRequest req, Long userId, String regionCode, String sidoCode, String sigunguCode, Long houseTypeCode, Long rentTypeCode, Long houseStatusCode, Long floorCode, Point position) {
        CurrentHouse house = new CurrentHouse();
        house.userId = userId;
        house.regionCode = regionCode;
        house.sidoCode = sidoCode;
        house.sigunguCode = sigunguCode;
        house.houseTypeCode = houseTypeCode;
        house.rentTypeCode = rentTypeCode;
        house.houseStatusCode = houseStatusCode;
        house.floorCode = floorCode;
        house.address = req.getAddress();
        house.position = position;
        house.deposit = req.getDeposit().intValue();
        house.monthlyCost = req.getMonthlyCost();
        house.managementCost = req.getManagementCost() != null ? req.getManagementCost() : 0;
        house.managementItems = req.getManagementItems() != null ? req.getManagementItems() : "";
        house.floorSize = req.getFloorSize();
        house.contractStart = req.getContractStartYearMonth();
        house.contractEnd = req.getContractEndYearMonth();
        house.buildYear = req.getBuildYear() != null ? req.getBuildYear() : 0;
        house.description = req.getDescription() != null ? req.getDescription() : "";
        house.viewCount = 0;
        house.soldAt = null;
        house.deletedAt = null;
        return house;
    }

}
