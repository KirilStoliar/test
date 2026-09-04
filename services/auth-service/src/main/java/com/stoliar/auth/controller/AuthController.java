package com.stoliar.auth.controller;

import com.stoliar.auth.dto.AuthResponse;
import com.stoliar.auth.dto.LoginRequest;
import com.stoliar.auth.dto.MessageResponse;
import com.stoliar.auth.dto.RefreshTokenRequest;
import com.stoliar.auth.dto.RegisterRequest;
import com.stoliar.auth.dto.UserInfoResponse;
import com.stoliar.auth.dto.VerifyTokenRequest;
import com.stoliar.auth.exception.InvalidCredentialsException;
import com.stoliar.auth.service.AuthService;
import com.stoliar.auth.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
        name = "Authentication",
        description = "Authentication and Authorization endpoints"
)
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService verificationService;

    public AuthController(
            AuthService authService,
            EmailVerificationService verificationService
    ) {
        this.authService = authService;
        this.verificationService = verificationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public MessageResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login to the system")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public AuthResponse refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return authService.refresh(
                request.refreshToken()
        );
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public MessageResponse logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return authService.logout(
                request.refreshToken()
        );
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify email address")
    public MessageResponse verify(
            @Valid @RequestBody VerifyTokenRequest request
    ) {
        verificationService.verify(request.token());

        return new MessageResponse(
                "Email successfully verified"
        );
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user info",
            description = """
                    Returns information about the currently
                    authenticated user including roles
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public UserInfoResponse getCurrentUser(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new InvalidCredentialsException();
        }

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new InvalidCredentialsException();
        }

        String userId = jwt.getSubject();

        String email = jwt.getClaim("email");

        Object rolesObject = jwt.getClaim("roles");

        List<String> roles = extractRoles(rolesObject);

        return new UserInfoResponse(
                userId,
                email,
                authentication.getName(),
                roles,
                authentication.isAuthenticated()
        );
    }

    private List<String> extractRoles(Object rolesObject) {

        if (rolesObject instanceof List<?> list) {
            return list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }

        if (rolesObject instanceof String role) {
            return List.of(role);
        }

        return List.of();
    }
}