package com.ssafy.backend.global.auth.service;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenRedisService {

    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";

    private final StringRedisTemplate stringRedisTemplate;

    public RefreshTokenRedisService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 사용자별 refresh token 을 TTL 과 함께 저장한다.
    // 키 하나에 "현재 유효한 refresh token 1개"만 남겨 두는 구조라서
    // 새로 로그인하거나 재발급되면 이전 토큰은 자연스럽게 대체된다.
    public void save(Long userId, String refreshToken, long expirationMillis) {
        stringRedisTemplate.opsForValue().set(
            buildKey(userId),
            refreshToken,
            Duration.ofMillis(expirationMillis)
        );
    }

    // 현재 사용자에게 마지막으로 발급된 refresh token 값을 읽어 온다.
    public String findByUserId(Long userId) {
        return stringRedisTemplate.opsForValue().get(buildKey(userId));
    }

    // 로그아웃이나 토큰 탈취 의심 상황이 발생하면 refresh token 을 즉시 제거한다.
    public void delete(Long userId) {
        stringRedisTemplate.delete(buildKey(userId));
    }

    private String buildKey(Long userId) {
        return REFRESH_TOKEN_PREFIX + userId;
    }
}
