package com.ssafy.backend.domain.user.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import com.ssafy.backend.domain.code.entity.CommonCodeDetail;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_need_lifestyle_tag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNeedLifestyleTag extends BaseEntity {

    @EmbeddedId
    private UserNeedLifestyleTagId id;

    @MapsId("userNeedId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_need_id", nullable = false)
    private UserNeed userNeed;

    @MapsId("codeDetailId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "code_detail_id", nullable = false)
    private CommonCodeDetail commonCodeDetail;

    @Builder
    public UserNeedLifestyleTag(UserNeedLifestyleTagId id, UserNeed userNeed, CommonCodeDetail commonCodeDetail) {
        this.id = id;
        this.userNeed = userNeed;
        this.commonCodeDetail = commonCodeDetail;
    }
}
