package com.ssafy.backend.global.cacheable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {
    private final List<CacheHandler> cacheHandlers;
    private final CacheKeyGenerator cacheKeyGenerator;
    private final Environment env;

    @Around("@annotation(myCacheable)")
    public Object handleCacheable(ProceedingJoinPoint joinPoint, MyCacheable myCacheable) {
        CacheStrategy cacheStrategy = myCacheable.cacheStrategy();
        CacheHandler cacheHandler = findCacheHandler(cacheStrategy);

        String key = cacheKeyGenerator.genKey(joinPoint, cacheStrategy, myCacheable.cacheName(), myCacheable.key());
        Duration ttl = Duration.ofSeconds(myCacheable.ttlSeconds());

        Supplier<Object> dataSourceSupplier = createDataSourceSupplier(joinPoint);
        Class returnType = findReturnType(joinPoint);

        try {
            log.info("[CacheAspect.handleCacheable] key={}", key);
            return cacheHandler.fetch(
                    key,
                    ttl,
                    dataSourceSupplier,
                    returnType
            );
        } catch (Exception e) {
            log.error("[CacheAspect.handleCacheable] key={}", key, e);
            return dataSourceSupplier.get();
        }
    }

    private CacheHandler findCacheHandler(CacheStrategy cacheStrategy) {
        return cacheHandlers.stream()
                .filter(handler -> handler.supports(cacheStrategy))
                .findFirst()
                .orElseThrow();
    }

    private Supplier<Object> createDataSourceSupplier(ProceedingJoinPoint joinPoint) {
        return () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new RuntimeException(e);
            }
        };
    }

    private Class findReturnType(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getReturnType();
    }

    @AfterReturning(pointcut = "@annotation(myCachePut)", returning = "result")
    public void handleCachePut(JoinPoint joinPoint, MyCachePut myCachePut, Object result) {
        CacheStrategy cacheStrategy = myCachePut.cacheStrategy();
        CacheHandler cacheHandler = findCacheHandler(cacheStrategy);
        String key = cacheKeyGenerator.genKey(joinPoint, cacheStrategy, myCachePut.cacheName(), myCachePut.key());
        log.info("[CacheAspect.handleCachePut] key={}", key);
        Duration ttl = Duration.ofSeconds(myCachePut.ttlSeconds());

        cacheHandler.put(key, ttl, result);
    }

    @AfterReturning(pointcut = "@annotation(myCacheEvict)")
    public void handleCacheEvict(JoinPoint joinPoint, MyCacheEvict myCacheEvict) {
        CacheStrategy cacheStrategy = myCacheEvict.cacheStrategy();
        CacheHandler cacheHandler = findCacheHandler(cacheStrategy);
        String key = cacheKeyGenerator.genKey(joinPoint, cacheStrategy, myCacheEvict.cacheName(), myCacheEvict.key());
        log.info("[CacheAspect.handleCacheEvict] key={}", key);
        cacheHandler.evict(key);
    }
}
