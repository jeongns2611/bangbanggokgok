package com.ssafy.backend.domain.user.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "user_need")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNeed extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "need_name", nullable = false)
    private String needName;

    @Column(name = "target_address", nullable = false, length = 256)
    private String targetAddress;

    @Column(name = "target_pos", columnDefinition = "geometry(Point, 4326)")
    private Point targetPos;

    @Column(name = "sido_name", nullable = false)
    private String sidoName;

    @Column(name = "sigungu_name", nullable = false)
    private String sigunguName;

    @Column(name = "max_commute_time", nullable = false)
    private Integer maxCommuteTime;

    @Column(name = "rent_type", nullable = false, length = 32)
    private String rentType;

    @Column(name = "min_deposit", nullable = false)
    private Integer minDeposit;

    @Column(name = "max_deposit")
    private Integer maxDeposit;

    @Column(name = "min_monthly_rent", nullable = false)
    private Integer minMonthlyRent;

    @Column(name = "max_monthly_rent")
    private Integer maxMonthlyRent;

    @Column(name = "min_exclusive_size", nullable = false, length = 10)
    private String minExclusiveSize;

    @Column(name = "is_default_need", nullable = false)
    private Boolean isDefaultNeed;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public UserNeed(
            User user,
            String needName,
            String targetAddress,
            Point targetPos,
            String sidoName,
            String sigunguName,
            Integer maxCommuteTime,
            String rentType,
            Integer minDeposit,
            Integer maxDeposit,
            Integer minMonthlyRent,
            Integer maxMonthlyRent,
            String minExclusiveSize,
            Boolean isDefaultNeed,
            LocalDateTime deletedAt,
            Integer createdBy,
            Integer updatedBy
    ) {
        this.user = user;
        this.needName = needName;
        this.targetAddress = targetAddress;
        this.targetPos = targetPos;
        this.sidoName = sidoName;
        this.sigunguName = sigunguName;
        this.maxCommuteTime = maxCommuteTime;
        this.rentType = rentType;
        this.minDeposit = minDeposit;
        this.maxDeposit = maxDeposit;
        this.minMonthlyRent = minMonthlyRent;
        this.maxMonthlyRent = maxMonthlyRent;
        this.minExclusiveSize = minExclusiveSize;
        this.isDefaultNeed = isDefaultNeed;
        this.deletedAt = deletedAt;
    }

    public void update(
            String needName,
            String targetAddress,
            Point targetPos,
            String sidoName,
            String sigunguName,
            Integer maxCommuteTime,
            String rentType,
            Integer minDeposit,
            Integer maxDeposit,
            Integer minMonthlyRent,
            Integer maxMonthlyRent,
            String minExclusiveSize
    ) {
        this.needName = needName;
        this.targetAddress = targetAddress;
        this.targetPos = targetPos;
        this.sidoName = sidoName;
        this.sigunguName = sigunguName;
        this.maxCommuteTime = maxCommuteTime;
        this.rentType = rentType;
        this.minDeposit = minDeposit;
        this.maxDeposit = maxDeposit;
        this.minMonthlyRent = minMonthlyRent;
        this.maxMonthlyRent = maxMonthlyRent;
        this.minExclusiveSize = minExclusiveSize;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void setDefaultNeed(Boolean isDefaultNeed) {
        this.isDefaultNeed = isDefaultNeed;
    }
}
