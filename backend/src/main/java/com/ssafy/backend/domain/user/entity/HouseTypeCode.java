package com.ssafy.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "house_type_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseTypeCode {

    @EmbeddedId
    private HouseTypeCodeId id;

    @MapsId("userNeedId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_need_id", nullable = false)
    private UserNeed userNeed;

    @Builder
    public HouseTypeCode(
            HouseTypeCodeId id,
            UserNeed userNeed
    ) {
        this.id = id;
        this.userNeed = userNeed;
    }
}
