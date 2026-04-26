package com.ssafy.backend.global.auth.jwt;

import com.ssafy.backend.domain.user.entity.User;
import com.ssafy.backend.domain.user.repository.UserRepository;
import com.ssafy.backend.global.auth.principal.CustomOAuth2User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties, UserRepository userRepository) {
        this.jwtProperties = jwtProperties;
        this.userRepository = userRepository;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    // access token 에는 사용자 식별 정보와 "이 토큰이 access 용도"라는 타입을 넣는다.
    // 서버는 이후 요청에서 이 타입을 보고 refresh token 이 API 인증에 쓰이는 것을 막는다.
    public String createAccessToken(Long userId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("userId", userId)
            .claim("email", email)
            .claim("type", "access")
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(jwtProperties.accessTokenExpiration())))
            .signWith(secretKey)
            .compact();
    }

    // refresh token 은 access token 보다 수명이 길고, 재발급 용도로만 사용한다.
    // API 인증에는 사용하지 않도록 type 을 refresh 로 고정한다.
    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("userId", userId)
            .claim("type", "refresh")
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(jwtProperties.refreshTokenExpiration())))
            .signWith(secretKey)
            .compact();
    }

    // JWT 서명과 만료 시간을 검증해서 위조되었거나 만료된 토큰을 걸러낸다.
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    // subject 에는 사용자 ID 를 넣어 두었기 때문에, 토큰만으로도 누가 보낸 요청인지 식별할 수 있다.
    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    // access / refresh 중 어떤 용도의 토큰인지 구분할 때 사용한다.
    public String getTokenType(String token) {
        return parseClaims(token).get("type", String.class);
    }

    // access token 의 만료 예정 시각에서 현재 시각을 빼서 "남은 유효 시간"을 계산한다.
    // 블랙리스트는 토큰이 원래 만료될 때까지만 보관하면 되므로 이 값이 필요하다.
    public long getRemainingValidityMillis(String token) {
        Date expiration = parseClaims(token).getExpiration();

        return expiration.getTime() - System.currentTimeMillis();
    }

    // 토큰의 사용자 정보를 기준으로 SecurityContext 에 넣을 인증 객체를 만든다.
    // 이 단계에서 DB 의 현재 사용자 정보를 다시 읽기 때문에,
    // 탈퇴 처리되었거나 존재하지 않는 사용자는 인증 객체를 만들 수 없다.
    public Authentication getAuthentication(String token) {
        Long userId = getUserId(token);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new JwtException("JWT 에 해당하는 사용자를 찾을 수 없습니다."));

        CustomOAuth2User principal = new CustomOAuth2User(
            userId,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
            Map.of(),
            "sub",
            user.getEmail(),
            user.getName(),
            user.getProfileUrl(),
            false
        );

        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }
}
