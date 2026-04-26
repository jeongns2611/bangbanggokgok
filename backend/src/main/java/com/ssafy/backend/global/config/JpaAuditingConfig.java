package com.ssafy.backend.global.config;

import java.util.Optional;
import com.ssafy.backend.global.auth.principal.CustomOAuth2User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    // 현재 사용자 식별자를 제공하는 Auditor 설정
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of(0L);
            }
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomOAuth2User customOAuth2User) {
                return Optional.of(customOAuth2User.getUserId());
            }
            return Optional.ofNullable(authentication.getName())
                .filter(name -> name.chars().allMatch(Character::isDigit))
                .map(Long::valueOf)
                .or(() -> Optional.of(0L));
        };
    }
}
