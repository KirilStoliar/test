package com.stoliar.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/manager")
@Tag(name = "Manager", description = "Manager endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ManagerController {

    @GetMapping("/panel")
    @Operation(summary = "Manager panel (requires ROLE_MANAGER or ROLE_ADMIN)")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public Map<String, String> managerPanel() {
        return Map.of(
                "message", "Welcome to Manager Panel",
                "access", "GRANTED",
                "level", "MANAGER"
        );
    }
}