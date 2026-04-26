package com.ssafy.backend.global.cacheable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyCacheEvict {
    CacheStrategy cacheStrategy();

    String cacheName();

    String key();
}
