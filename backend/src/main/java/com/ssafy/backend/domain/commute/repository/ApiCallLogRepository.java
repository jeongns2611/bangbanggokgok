package com.ssafy.backend.domain.commute.repository;

import com.ssafy.backend.domain.commute.entity.ApiCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ApiCallLogRepository extends JpaRepository<ApiCallLog, Long> {
    
    /**
     * 특정 날짜의 API 호출 로그를 조회한다.
     */
    Optional<ApiCallLog> findByReferenceDate(LocalDate referenceDate);
}
