package com.stoliar.auth.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenCleanupService {

    private final EmailVerificationService emailVerificationService;

    public AuthTokenCleanupService(
            EmailVerificationService emailVerificationService
    ) {
        this.emailVerificationService = emailVerificationService;
    }

    @Scheduled(
            fixedDelayString = "${security.cleanup.email-verification-delay:3600000}"
    )
    public void cleanupExpiredEmailVerificationTokens() {

        emailVerificationService.deleteExpiredTokens();
    }
}