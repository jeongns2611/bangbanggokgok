package com.ssafy.backend.global.auth.service;

import com.ssafy.backend.domain.user.entity.User;
import com.ssafy.backend.domain.user.repository.UserRepository;
import com.ssafy.backend.global.auth.jwt.JwtProperties;
import com.ssafy.backend.global.auth.jwt.JwtTokenProvider;
import com.ssafy.backend.global.auth.jwt.TokenRefreshResponse;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRedisService refreshTokenRedisService;
    private final AccessTokenBlacklistRedisService accessTokenBlacklistRedisService;

    @Transactional
    public TokenRefreshResponse refreshTokens(String refreshToken) {
        Claims claims;
        // 1. 토큰 자체가 위조되었거나 이미 만료되었는지 먼저 검사한다.
        try {
            claims = jwtTokenProvider.parseClaims(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다.");
        }

        // 2. access token 을 실수로 재발급 API 에 보내는 경우를 막는다.
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "리프레시 토큰이 아닙니다.");
        }

        Long userId = Long.valueOf(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "토큰에 해당하는 사용자를 찾을 수 없습니다."));

        String savedRefreshToken = refreshTokenRedisService.findByUserId(userId);
        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            // RTR 핵심: 저장된 "현재 refresh token" 과 다르면 이전 토큰 재사용으로 간주한다.
            // 이런 상황은 토큰 탈취나 중복 사용 가능성이 있으므로 현재 세션의 refresh token 도 제거한다.
            refreshTokenRedisService.delete(userId);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "리프레시 토큰이 일치하지 않거나 이미 재발급되었습니다.");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(userId, user.getEmail());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

        // 새 refresh token 을 저장하면서 이전 refresh token 은 덮어쓴다.
        // 이 동작 자체가 RTR(Rotate Refresh Token) 이다.
        refreshTokenRedisService.save(userId, newRefreshToken, jwtProperties.refreshTokenExpiration());

        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String accessToken) {
        // 1. access token 이 유효하지 않으면 로그아웃 처리도 진행하지 않는다.
        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다.");
        }

        // 2. refresh token 을 실수로 로그아웃 API 에 보내는 경우를 막는다.
        if (!"access".equals(jwtTokenProvider.getTokenType(accessToken))) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "액세스 토큰이 아닙니다.");
        }

        Long userId = jwtTokenProvider.getUserId(accessToken);

        // 3. 이 사용자의 refresh token 을 제거해서 재발급 경로를 차단한다.
        refreshTokenRedisService.delete(userId);

        // 4. 이미 발급된 access token 도 남은 만료 시간 동안 블랙리스트에 넣어서 재사용을 막는다.
        long remainingValidityMillis = jwtTokenProvider.getRemainingValidityMillis(accessToken);
        accessTokenBlacklistRedisService.blacklist(accessToken, remainingValidityMillis);
    }
}
