package com.stoliar.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        prefix = "mail",
        name = "enabled",
        havingValue = "true"
)
public class SmtpMailService implements MailService {

    private final JavaMailSender mailSender;
    private final String from;
    private final String verificationBaseUrl;

    public SmtpMailService(
            JavaMailSender mailSender,
            @Value(
                    "${mail.from}"
            ) String from,
            @Value(
                    "${mail.verification-base-url}"
            ) String verificationBaseUrl
    ) {
        this.mailSender = mailSender;
        this.from = from;
        this.verificationBaseUrl = verificationBaseUrl;
    }

    @Override
    public void sendEmailVerification(
            String email,
            String verificationToken
    ) {
        String verificationUrl =
                verificationBaseUrl
                        + "?token="
                        + verificationToken;

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(email);
        message.setSubject(
                "Service Orders - Email verification"
        );

        message.setText(
                """
                Hello!

                Please verify your email address by opening the link below:

                %s

                This link will expire according to the configured
                email verification token lifetime.

                If you did not create an account, you can ignore this email.
                """.formatted(verificationUrl)
        );

        mailSender.send(message);
    }
}