package com.ssafy.backend.batch.repository;

import com.ssafy.backend.batch.entity.OutboxStatus;
import com.ssafy.backend.batch.entity.VectorOutbox;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OutboxRepository extends JpaRepository<VectorOutbox, Long> {

    // 특정 상태(예: READY)의 Outbox 이벤트를 조회
    List<VectorOutbox> findByStatus(OutboxStatus status);

    Optional<VectorOutbox> findByHouseId(Long houseId);

    // 배치 성능을 위해 특정 상태의 이벤트를 지정된 수만큼만 조회 (Limit 적용)
    List<VectorOutbox> findByStatus(OutboxStatus status, Pageable pageable);

    // 배열 조건으로 조회하기 위한 메서드
    List<VectorOutbox> findByStatusIn(List<OutboxStatus> statuses, Pageable pageable);

    // 재시도 횟수가 특정 값 미만이고, 특정 상태인 항목 조회 (재처리 로직용)
    List<VectorOutbox> findByStatusAndRetryCountLessThan(OutboxStatus status, Integer retryCount);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM VectorOutbox v WHERE v.status = :status AND v.createdAt < :beforeDate")
    void deleteByStatusAndCreatedAtBefore(OutboxStatus status, LocalDateTime beforeDate);
}
