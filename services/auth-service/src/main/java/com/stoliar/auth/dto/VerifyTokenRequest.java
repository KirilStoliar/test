package com.stoliar.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyTokenRequest(

        @NotBlank
        String token
) {
}