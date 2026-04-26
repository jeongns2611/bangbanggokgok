package com.ssafy.backend.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserNeedHouseTypeId implements Serializable {

    @Column(name = "user_need_id")
    private Long userNeedId;

    @Column(name = "code_detail_id")
    private Long codeDetailId;
}
