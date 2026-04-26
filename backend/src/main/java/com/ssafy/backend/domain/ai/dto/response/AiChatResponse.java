package com.ssafy.backend.domain.ai.dto.response;

import com.ssafy.backend.domain.house.dto.response.CurrentHouseListResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    String answer;
    List<CurrentHouseListResponse.ListElement> data;
}
