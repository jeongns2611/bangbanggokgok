package com.ssafy.backend.domain.house.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddressValidResponse {
    private boolean valid;
    private String userMessage;
}
