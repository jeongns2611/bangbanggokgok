package com.ssafy.backend.global.auth.service;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenBlacklistRedisService {

    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "auth:blacklist:";
    private static final String BLACKLIST_VALUE = "logout";

    private final StringRedisTemplate stringRedisTemplate;

    public AccessTokenBlacklistRedisService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 로그아웃된 access token 을 Redis 에 남은 만료 시간만큼 저장한다.
    // 이렇게 해두면 토큰 자체는 아직 만료되지 않았더라도 서버에서 더 이상 받아주지 않는다.
    public void blacklist(String accessToken, long expirationMillis) {
        if (expirationMillis <= 0) {
            return;
        }

        stringRedisTemplate.opsForValue().set(
            buildKey(accessToken),
            BLACKLIST_VALUE,
            Duration.ofMillis(expirationMillis)
        );
    }

    // 요청으로 들어온 access token 이 이미 로그아웃 처리된 토큰인지 확인한다.
    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(buildKey(accessToken)));
    }

    private String buildKey(String accessToken) {
        return ACCESS_TOKEN_BLACKLIST_PREFIX + accessToken;
    }
}
