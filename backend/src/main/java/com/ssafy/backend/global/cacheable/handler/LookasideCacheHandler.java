package com.ssafy.backend.global.cacheable.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.global.cacheable.CacheHandler;
import com.ssafy.backend.global.cacheable.CacheStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookasideCacheHandler implements CacheHandler {

    private static final int MAX_CACHE_SIZE = 1000;
    // Lua Script 정의
    // KEYS[1]: Data Key ({strategy}:{name}:{key})
    // KEYS[2]: Metadata Key ({strategy}:{name}:metadata)
    // ARGV[1]: Value (JSON)
    // ARGV[2]: TTL (Milliseconds)
    // ARGV[3]: Expiration Timestamp (Score)
    // ARGV[4]: Max Cache Size
    // ARGV[5]: Current Timestamp
    private static final String SCRIPT_TEXT =
            "local dataKey = KEYS[1] " +
                    "local metaKey = KEYS[2] " +
                    "local value = ARGV[1] " +
                    "local ttl = tonumber(ARGV[2]) " +
                    "local score = tonumber(ARGV[3]) " +
                    "local maxSize = tonumber(ARGV[4]) " +
                    "local now = tonumber(ARGV[5]) " +

                    // 1. 데이터 저장 (SET key value PX ttl)
                    "if ttl > 0 then " +
                    "   redis.call('SET', dataKey, value, 'PX', ttl) " +
                    "else " +
                    "   redis.call('SET', dataKey, value) " +
                    "end " +

                    // 2. 메타데이터 추가 (ZADD)
                    "redis.call('ZADD', metaKey, score, dataKey) " +

                    // 3. 이미 만료된 메타데이터 정리 (Lazy Cleanup)
                    "redis.call('ZREMRANGEBYSCORE', metaKey, 0, now) " +

                    // 4. 개수 제한 확인 및 삭제 (오래된 것부터)
                    "local size = redis.call('ZCARD', metaKey) " +
                    "if size > maxSize then " +
                    "   local removeCount = size - maxSize " +
                    "   local keysToRemove = redis.call('ZRANGE', metaKey, 0, removeCount - 1) " +
                    "   for i, k in ipairs(keysToRemove) do " +
                    "       redis.call('DEL', k) " +
                    "       redis.call('ZREM', metaKey, k) " +
                    "   end " +
                    "end " +

                    // 5. 메타데이터 키 자체의 만료 시간 갱신 (7일)
                    "redis.call('EXPIRE', metaKey, 604800) " +
                    "return nil";
    private static final RedisScript<Void> PUT_SCRIPT = new DefaultRedisScript<>(SCRIPT_TEXT, Void.class);
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

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

        int lastColonIndex = key.lastIndexOf(":");
        // 그룹 키를 생성할 수 없는 경우 일반 SET만 수행하도록 예외 처리도 가능하지만,
        // 여기서는 키 형식이 항상 {strategy}:{name}:{key}라고 가정합니다.
        if (lastColonIndex == -1) return;

        String groupKey = key.substring(0, lastColonIndex) + ":metadata";

        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            long ttlMillis = ttl.toMillis();
            long expirationTime = System.currentTimeMillis() + ttlMillis;

            log.info("[LookAside.put] executing Lua Script. key={}, ttl={}ms", key, ttlMillis);

            // Lua Script 실행
            stringRedisTemplate.execute(
                    PUT_SCRIPT,
                    List.of(key, groupKey), // KEYS
                    jsonValue,              // ARGV[1]
                    String.valueOf(ttlMillis),      // ARGV[2]
                    String.valueOf(expirationTime), // ARGV[3]
                    String.valueOf(MAX_CACHE_SIZE), // ARGV[4]
                    String.valueOf(System.currentTimeMillis()) // ARGV[5]
            );

        } catch (JsonProcessingException e) {
            log.error("[LookAside.put] Serialization Error! key={}", key, e);
        }
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

