package com.stoliar.auth.service;

public interface MailService {

    void sendEmailVerification(
            String email,
            String verificationToken
    );
}