package com.stoliar.user.controller;

import com.stoliar.user.dto.CreateUserRequest;
import com.stoliar.user.dto.UpdateUserRequest;
import com.stoliar.user.dto.UserResponse;
import com.stoliar.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('MANAGER', 'ADMIN') or " +
                    "#id.toString() == authentication.tokenAttributes['sub']"
    )
    public UserResponse findById(
            @PathVariable UUID id
    ) {
        return userService.findById(id);
    }

    @GetMapping("/by-email")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public UserResponse findByEmail(
            @RequestParam String email
    ) {
        return userService.findByEmail(email);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public UserResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id
    ) {
        userService.delete(id);
    }
}