package com.stoliar.auth.exception;

public class EmailSendingException extends RuntimeException {

    public EmailSendingException() {
        super("Failed to send verification email");
    }

    public EmailSendingException(Throwable cause) {
        super("Failed to send verification email", cause);
    }
}