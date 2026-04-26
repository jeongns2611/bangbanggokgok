package com.ssafy.backend.domain.user.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "lifestyle_tag_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LifestyleTagCode {

    @EmbeddedId
    private LifestyleTagCodeId id;

    @Builder
    public LifestyleTagCode(LifestyleTagCodeId id) {
        this.id = id;
    }
}
