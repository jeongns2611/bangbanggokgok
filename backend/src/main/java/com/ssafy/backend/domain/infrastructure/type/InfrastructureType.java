package com.ssafy.backend.domain.infrastructure.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InfrastructureType {

    BUS("BUS"),
    SUBWAY("SUBWAY"),
    CONVENIENCE("CONVENIENCE"),
    LAUNDRY("LAUNDRY"),
    CAFE("CAFE"),
    HOSPITAL("HOSPITAL"),
    PHARMACY("PHARMACY");

    private final String code;
}

