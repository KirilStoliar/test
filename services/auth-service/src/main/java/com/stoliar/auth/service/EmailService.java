package com.stoliar.auth.service;

import com.stoliar.auth.entity.AuthUser;
import com.stoliar.auth.exception.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    private static final Logger log =
            LoggerFactory.getLogger(EmailService.class);

    private static final String VERIFICATION_SUBJECT =
            "Verify your Service Orders email address";

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;
    private final String verificationBaseUrl;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${mail.enabled:false}") boolean enabled,
            @Value("${mail.from:no-reply@service-orders.local}") String from,
            @Value("${mail.verification-base-url:http://localhost:8081/api/v1/auth/verify}")
            String verificationBaseUrl
    ) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
        this.verificationBaseUrl = verificationBaseUrl;
    }

    public void sendVerificationEmail(
            AuthUser user,
            String rawToken
    ) {
        if (!enabled) {
            log.info(
                    "Email sending is disabled. Verification email was not sent to {}",
                    user.getEmail()
            );
            return;
        }

        String verificationUrl =
                UriComponentsBuilder
                        .fromUriString(verificationBaseUrl)
                        .queryParam("token", rawToken)
                        .build()
                        .encode()
                        .toUriString();

        String text = """
                Hello,

                Please verify your Service Orders account by opening the following link:

                %s

                This verification link is valid for the configured verification token lifetime.

                If you did not create this account, you can safely ignore this email.

                Service Orders
                """.formatted(verificationUrl);

        try {
            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            false,
                            StandardCharsets.UTF_8.name()
                    );

            helper.setFrom(from);
            helper.setTo(user.getEmail());
            helper.setSubject(VERIFICATION_SUBJECT);
            helper.setText(text, false);

            mailSender.send(message);

            log.info(
                    "Verification email sent to {}",
                    user.getEmail()
            );
        } catch (MessagingException | MailException exception) {
            log.error(
                    "Failed to send verification email to {}",
                    user.getEmail(),
                    exception
            );

            throw new EmailSendingException();
        }
    }
}