package com.stoliar.auth.service;

import com.stoliar.auth.entity.AuthUser;
import com.stoliar.auth.entity.RefreshToken;
import com.stoliar.auth.exception.InvalidRefreshTokenException;
import com.stoliar.auth.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final Duration refreshTokenTtl;
    private final SecureRandom secureRandom;

    public RefreshTokenService(
            RefreshTokenRepository repository,
            @Value("${security.jwt.refresh-token-ttl:30d}")
            Duration refreshTokenTtl
    ) {
        this.repository = repository;
        this.refreshTokenTtl = refreshTokenTtl;
        this.secureRandom = new SecureRandom();
    }

    @Transactional
    public String create(AuthUser user) {

        Instant now = Instant.now();

        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        String tokenHash = hash(rawToken);

        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID(),
                user,
                tokenHash,
                now.plus(refreshTokenTtl),
                now
        );

        repository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public AuthUser validateAndRotate(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        String tokenHash = hash(rawToken);

        RefreshToken token = repository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(
                        InvalidRefreshTokenException::new
                );

        if (token.isExpired() || token.isRevoked()) {
            throw new InvalidRefreshTokenException();
        }

        AuthUser user = token.getUser();

        token.revoke();

        return user;
    }

    @Transactional
    public void revoke(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        String tokenHash = hash(rawToken);

        RefreshToken token = repository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(
                        InvalidRefreshTokenException::new
                );

        if (token.isExpired() || token.isRevoked()) {
            throw new InvalidRefreshTokenException();
        }

        token.revoke();
    }

    private String hash(String value) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }
}