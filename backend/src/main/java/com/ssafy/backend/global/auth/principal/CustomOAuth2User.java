package com.ssafy.backend.global.auth.principal;

import java.util.Collection;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * GrantedAuthority
 *
 * - Spring Security에서 제공하는 사용자가 가진 권한/역할
 *
 * - controller에 @PreAuthorize("hasRole('ADMIN')") 코드를 사용해 API 사용 권한 검사 가능
 * - 선택사항이기 때문에 모든 api에 할 필요 없음
 *
 * - 우리 프로젝트는 권한을 따로 나누지 않았기 때문에 사용할 필요 없음
 *
 */
@Getter
public class CustomOAuth2User implements OAuth2User {

    private final Long userId;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final String email;
    private final String displayName;
    private final String profileUrl;
    private final boolean newUser;

    public CustomOAuth2User(
        Long userId,
        Collection<? extends GrantedAuthority> authorities,
        Map<String, Object> attributes,
        String nameAttributeKey,
        String email,
        String displayName,
        String profileUrl,
        boolean newUser
    ) {
        this.userId = userId;
        this.authorities = authorities;
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.email = email;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
        this.newUser = newUser;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
