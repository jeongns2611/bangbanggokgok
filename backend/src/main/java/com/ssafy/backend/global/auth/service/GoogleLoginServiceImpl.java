package com.ssafy.backend.global.auth.service;

import com.ssafy.backend.domain.user.entity.User;
import com.ssafy.backend.domain.user.repository.UserRepository;
import com.ssafy.backend.global.auth.dto.response.GoogleTokenInfoResponseDto;
import com.ssafy.backend.global.auth.dto.response.LoginAuthResponse;
import com.ssafy.backend.global.auth.jwt.JwtProperties;
import com.ssafy.backend.global.auth.jwt.JwtTokenProvider;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoogleLoginServiceImpl implements GoogleLoginService {

    private static final String GOOGLE_PROVIDER = "google";

    private final WebClient webClient;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRedisService refreshTokenRedisService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Override
    @Transactional
    public LoginAuthResponse login(String googleToken) {
        log.info("Google login requested");
        GoogleTokenInfoResponseDto tokenInfo = fetchGoogleTokenInfo(googleToken);
        validateTokenInfo(tokenInfo);

        boolean isNewUser = userRepository.findByProviderAndSub(GOOGLE_PROVIDER, tokenInfo.sub()).isEmpty();
        User user = userRepository.findByProviderAndSub(GOOGLE_PROVIDER, tokenInfo.sub())
            .map(existingUser -> {
                existingUser.updateProfile(tokenInfo.email(), tokenInfo.name(), tokenInfo.picture());
                return existingUser;
            })
            .orElseGet(() -> userRepository.save(
                User.builder()
                    .email(tokenInfo.email())
                    .provider(GOOGLE_PROVIDER)
                    .sub(tokenInfo.sub())
                    .name(tokenInfo.name())
                    .profileUrl(tokenInfo.picture())
                    .build()
            ));

        Long userId = user.getId().longValue();
        String accessToken = jwtTokenProvider.createAccessToken(userId, user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        refreshTokenRedisService.save(userId, refreshToken, jwtProperties.refreshTokenExpiration());
        log.info("Google login completed userId={} email={} isNewUser={}", userId, user.getEmail(), isNewUser);

        return new LoginAuthResponse(
            userId,
            user.getEmail(),
            user.getName(),
            user.getProfileUrl(),
            accessToken,
            refreshToken,
            isNewUser
        );
    }

    private GoogleTokenInfoResponseDto fetchGoogleTokenInfo(String googleToken) {
        try {
            GoogleTokenInfoResponseDto tokenInfo = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .scheme("https")
                    .host("oauth2.googleapis.com")
                    .path("/tokeninfo")
                    .queryParam("id_token", googleToken)
                    .build()
                )
                .retrieve()
                .bodyToMono(GoogleTokenInfoResponseDto.class)
                .block();

            if (tokenInfo == null) {
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
            }

            return tokenInfo;
        } catch (WebClientResponseException.BadRequest | WebClientResponseException.Unauthorized e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않은 Google 토큰입니다.");
        } catch (WebClientResponseException e) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private void validateTokenInfo(GoogleTokenInfoResponseDto tokenInfo) {
        if (!googleClientId.equals(tokenInfo.aud())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Google 토큰 aud가 일치하지 않습니다.");
        }

        if (!"true".equalsIgnoreCase(tokenInfo.emailVerified())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Google 이메일 검증이 완료되지 않았습니다.");
        }

        if (isBlank(tokenInfo.sub()) || isBlank(tokenInfo.email()) || isBlank(tokenInfo.name())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Google 사용자 정보가 불완전합니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
