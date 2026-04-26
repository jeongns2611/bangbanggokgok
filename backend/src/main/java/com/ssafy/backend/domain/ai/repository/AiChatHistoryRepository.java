package com.ssafy.backend.domain.ai.repository;

import com.ssafy.backend.domain.ai.entity.AiChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiChatHistoryRepository extends JpaRepository<AiChatHistory, Long> {
    List<AiChatHistory> findTop3ByCreatedByOrderByCreatedAtDesc(Long createdBy);
}
