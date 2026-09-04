package com.stoliar.user.service;

import com.stoliar.events.user.UserCreated;
import com.stoliar.user.dto.CreateUserRequest;
import com.stoliar.user.dto.UpdateUserRequest;
import com.stoliar.user.dto.UserResponse;
import com.stoliar.user.entity.UserEntity;
import com.stoliar.user.exception.UserAlreadyExistsException;
import com.stoliar.user.exception.UserNotFoundException;
import com.stoliar.user.model.Role;
import com.stoliar.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserService {

    private static final Role DEFAULT_ROLE = Role.ROLE_USER;

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .map(UserResponse::from)
                .orElseThrow(() ->
                        new UserNotFoundException(email)
                );
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserAlreadyExistsException(email);
        }

        UserEntity user = new UserEntity(
                UUID.randomUUID(),
                email,
                request.role(),
                Instant.now()
        );

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(
            UUID userId,
            UpdateUserRequest request
    ) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String email = normalizeEmail(request.email());

        if (!email.equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserAlreadyExistsException(email);
        }

        user.updateEmail(email);
        user.updateRole(request.role());

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        userRepository.deleteById(userId);
    }

    /**
     * Kafka event from auth-service.
     *
     * This method is intentionally idempotent.
     * Re-delivery of the same event must not create
     * a duplicate user.
     */
    @Transactional
    public void createUser(UserCreated event) {

        UUID userId = UUID.fromString(event.getUserId());
        String email = normalizeEmail(event.getEmail());

        if (userRepository.existsById(userId)) {
            return;
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        UserEntity user = new UserEntity(
                userId,
                email,
                DEFAULT_ROLE,
                event.getCreatedAt()
        );

        userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}