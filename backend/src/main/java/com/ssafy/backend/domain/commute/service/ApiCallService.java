package com.ssafy.backend.domain.commute.service;

import com.ssafy.backend.domain.commute.entity.ApiCallLog;
import com.ssafy.backend.domain.commute.repository.ApiCallLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiCallService {

    private static final int API_CALL_LIMIT = 500;
    private final ApiCallLogRepository apiCallLogRepository;

    /**
     * 오늘 기준 API 호출이 가능한 상태인지 확인한다.
     */
    @Transactional(readOnly = true)
    public boolean isLimitReached() {
        int currentCount = getTodayCallCount();
        log.info("현재 ODSay API 호출 횟수: {} / 제한: {}", currentCount, API_CALL_LIMIT);
        return currentCount >= API_CALL_LIMIT;
    }

    /**
     * 오늘 기준 API 호출 횟수를 1 증가시킨다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementCallCount() {
        LocalDate today = LocalDate.now();
        ApiCallLog logEntry = apiCallLogRepository.findByReferenceDate(today)
                .orElseGet(() -> apiCallLogRepository.save(ApiCallLog.of(today, 0)));

        logEntry.increment();
        log.debug("ODsay API 호출 횟수 증가: {} -> {}", today, logEntry.getCount());
    }

    private int getTodayCallCount() {
        return apiCallLogRepository.findByReferenceDate(LocalDate.now())
                .map(ApiCallLog::getCount)
                .orElse(0);
    }
}
