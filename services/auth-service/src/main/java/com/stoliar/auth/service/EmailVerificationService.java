package com.stoliar.auth.service;

import com.stoliar.auth.entity.AuthUser;
import com.stoliar.auth.entity.EmailVerificationToken;
import com.stoliar.auth.exception.InvalidCredentialsException;
import com.stoliar.auth.repository.EmailVerificationTokenRepository;
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
public class EmailVerificationService {

    private final EmailVerificationTokenRepository repository;
    private final Duration tokenTtl;
    private final SecureRandom secureRandom;

    public EmailVerificationService(
            EmailVerificationTokenRepository repository,
            @Value("${security.jwt.email-verification-ttl:24h}")
            Duration tokenTtl
    ) {
        this.repository = repository;
        this.tokenTtl = tokenTtl;
        this.secureRandom = new SecureRandom();
    }

    @Transactional
    public String createToken(AuthUser user) {

        repository.deleteByUserId(user.getId());

        Instant now = Instant.now();

        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);

        String rawToken =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(bytes);

        String tokenHash =
                hashToken(rawToken);

        EmailVerificationToken token =
                new EmailVerificationToken(
                        UUID.randomUUID(),
                        user,
                        tokenHash,
                        now.plus(tokenTtl),
                        now
                );

        repository.save(token);

        return rawToken;
    }

    @Transactional
    public void verify(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidCredentialsException();
        }

        String tokenHash =
                hashToken(rawToken);

        EmailVerificationToken token =
                repository
                        .findByTokenHashForUpdate(tokenHash)
                        .orElseThrow(
                                InvalidCredentialsException::new
                        );

        if (token.isExpired()) {

            repository.delete(token);

            throw new InvalidCredentialsException();
        }

        AuthUser user = token.getUser();

        if (user.isEmailVerified()) {

            repository.delete(token);

            return;
        }

        user.verifyEmail();

        repository.delete(token);
    }

    @Transactional
    public int deleteExpiredTokens() {

        return repository.deleteExpiredTokens(
                Instant.now()
        );
    }

    private String hashToken(String value) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return Base64.getEncoder()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "SHA-256 algorithm not available",
                    exception
            );
        }
    }
}