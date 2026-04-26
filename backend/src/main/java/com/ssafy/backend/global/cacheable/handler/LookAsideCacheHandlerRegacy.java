package com.ssafy.backend.global.cacheable.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.global.cacheable.CacheHandler;
import com.ssafy.backend.global.cacheable.CacheStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;
import java.util.function.Supplier;

// Lua Script 네트워크 최적화 없는 버전 (읽기편함)

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAsideCacheHandlerRegacy implements CacheHandler {

    private static final int MAX_CACHE_SIZE = 1000; // 그룹당 최대 캐시 수
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper; // JSON 변환을 위해 주입

    @Override
    public <T> T fetch(String key, Duration ttl, Supplier<T> dataSourceSupplier, Class<T> clazz) {
        String cachedJson = stringRedisTemplate.opsForValue().get(key);

        if (cachedJson != null) {
            try {
                log.info("[LookAside.fetch] Cache Hit! key={}", key);
                return objectMapper.readValue(cachedJson, clazz);
            } catch (JsonProcessingException e) {
                log.error("[LookAside.fetch] Deserialization Error! key={}", key, e);
            }
        }

        log.info("[LookAside.fetch] Cache Miss... fetching from DB. key={}", key);
        T result = dataSourceSupplier.get();

        if (result != null) {
            put(key, ttl, result);
        }

        return result;
    }

    @Override
    public void put(String key, Duration ttl, Object value) {
        if (value == null) return;

        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            log.info("[LookAside.put] key={}, ttl={}s", key, ttl.getSeconds());

            if (ttl.isZero() || ttl.isNegative()) {
                stringRedisTemplate.opsForValue().set(key, jsonValue);
            } else {
                stringRedisTemplate.opsForValue().set(key, jsonValue, ttl);
                manageCacheSize(key, ttl);
            }
        } catch (JsonProcessingException e) {
            log.error("[LookAside.put] Serialization Error! key={}", key, e);
        }
    }

    private void manageCacheSize(String key, Duration ttl) {
        int lastColonIndex = key.lastIndexOf(":");
        if (lastColonIndex == -1) return;

        String groupKey = key.substring(0, lastColonIndex) + ":metadata";
        long expirationTime = System.currentTimeMillis() + ttl.toMillis();

        // 1. ZSet Add (Score: 만료 시각)
        stringRedisTemplate.opsForZSet().add(groupKey, key, expirationTime);

        // 2. Remove Expired Metadata (이미 만료된 키 정리)
        stringRedisTemplate.opsForZSet().removeRangeByScore(groupKey, 0, System.currentTimeMillis());

        // 3. Check Size
        Long size = stringRedisTemplate.opsForZSet().zCard(groupKey);
        if (size != null && size > MAX_CACHE_SIZE) {
            long removeCount = size - MAX_CACHE_SIZE;

            // 4. 삭제 대상 조회 (만료 시간이 가장 임박한 것부터)
            Set<String> keysToRemove = stringRedisTemplate.opsForZSet().range(groupKey, 0, removeCount - 1);

            if (keysToRemove != null && !keysToRemove.isEmpty()) {
                // 실제 데이터 삭제
                stringRedisTemplate.delete(keysToRemove);
                // 메타데이터 삭제
                stringRedisTemplate.opsForZSet().remove(groupKey, keysToRemove.toArray());
                log.info("[LookAside.manageCacheSize] Evicted {} keys. groupKey={}", keysToRemove.size(), groupKey);
            }
        }
        // 메타데이터 키 만료 설정 (넉넉하게)
        stringRedisTemplate.expire(groupKey, Duration.ofDays(7));
    }

    @Override
    public void evict(String key) {
        log.info("[LookAside.evict] key={}", key);
        stringRedisTemplate.delete(key);

        int lastColonIndex = key.lastIndexOf(":");
        if (lastColonIndex != -1) {
            String groupKey = key.substring(0, lastColonIndex) + ":metadata";
            stringRedisTemplate.opsForZSet().remove(groupKey, key);
        }
    }

    @Override
    public boolean supports(CacheStrategy cacheStrategy) {
        return CacheStrategy.LOOK_ASIDE == cacheStrategy;
    }
}
