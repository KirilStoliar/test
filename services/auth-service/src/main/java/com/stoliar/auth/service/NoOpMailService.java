package com.stoliar.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        prefix = "mail",
        name = "enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class NoOpMailService implements MailService {

    private static final Logger log =
            LoggerFactory.getLogger(NoOpMailService.class);

    @Override
    public void sendEmailVerification(
            String email,
            String verificationToken
    ) {
        /*
         * Deliberately do not log the verification token.
         *
         * In local development an SMTP test server such as MailHog
         * or Mailpit should be used instead.
         */
        log.info(
                "Email verification message prepared for {}",
                email
        );
    }
}