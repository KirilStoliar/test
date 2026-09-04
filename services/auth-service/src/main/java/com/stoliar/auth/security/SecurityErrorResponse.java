package com.stoliar.auth.security;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SecurityErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}