package com.stoliar.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleEmailExists(
            EmailAlreadyExistsException exception
    ) {
        return Map.of(
                "status", 409,
                "error", "Conflict",
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        return Map.of(
                "status", 401,
                "error", "Unauthorized",
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleInvalidRefreshToken(
            InvalidRefreshTokenException exception
    ) {
        return Map.of(
                "status", 401,
                "error", "Unauthorized",
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(EmailSendingException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, Object> handleEmailSending(
            EmailSendingException exception
    ) {
        return Map.of(
                "status", 503,
                "error", "Service Unavailable",
                "message", exception.getMessage()
        );
    }
}