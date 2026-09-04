package com.stoliar.auth.controller;

import com.stoliar.auth.dto.CreateUserWithRoleRequest;
import com.stoliar.auth.dto.MessageResponse;
import com.stoliar.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/admin")
@Tag(name = "Admin", description = "Admin endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Admin dashboard (requires ROLE_ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> adminDashboard() {
        return Map.of(
                "message", "Welcome to Admin Dashboard",
                "access", "GRANTED",
                "level", "ADMIN"
        );
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create user with role (requires ROLE_ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public MessageResponse createUserWithRole(
            @Valid @RequestBody CreateUserWithRoleRequest request
    ) {
        return authService.createUserWithRole(request);
    }
}