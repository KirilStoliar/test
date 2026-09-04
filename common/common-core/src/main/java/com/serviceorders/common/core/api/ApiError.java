package com.serviceorders.common.core.api;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {

    public ApiError {
        details = details == null ? List.of() : List.copyOf(details);
    }

    public static ApiError of(
            int status,
            String error,
            String message,
            String path
    ) {
        return new ApiError(
                Instant.now(),
                status,
                error,
                message,
                path,
                List.of()
        );
    }
}