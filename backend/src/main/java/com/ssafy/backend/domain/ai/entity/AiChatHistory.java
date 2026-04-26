package com.ssafy.backend.domain.ai.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "ai_chat_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiChatHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_message", nullable = false, columnDefinition = "TEXT")
    private String userMessage;

    @Column(name = "ai_message", columnDefinition = "TEXT")
    private String aiMessage;

    @Builder
    public AiChatHistory(
            String userMessage,
            String aiMessage
    ) {
        this.userMessage = userMessage;
        this.aiMessage = aiMessage;
    }
}
