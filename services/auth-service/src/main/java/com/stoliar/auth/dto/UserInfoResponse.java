package com.stoliar.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "User information response")
public record UserInfoResponse(

        @Schema(description = "User ID", example = "a39e0a70-7c0b-4639-8dd9-348f2a7ff227")
        String userId,

        @Schema(description = "User email", example = "string@mail.ru")
        String email,

        @Schema(description = "Username", example = "string@mail.ru")
        String username,

        @Schema(description = "User roles", example = "[\"ROLE_USER\"]")
        List<String> roles,

        @Schema(description = "Is user authenticated", example = "true")
        boolean authenticated
) {
}