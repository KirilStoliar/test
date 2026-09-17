package com.stoliar.auth.service;

import com.stoliar.auth.dto.AuthResponse;
import com.stoliar.auth.dto.CreateUserWithRoleRequest;
import com.stoliar.auth.dto.LoginRequest;
import com.stoliar.auth.dto.MessageResponse;
import com.stoliar.auth.dto.RegisterRequest;
import com.stoliar.auth.entity.AuthUser;
import com.stoliar.auth.exception.EmailAlreadyExistsException;
import com.stoliar.auth.exception.InvalidCredentialsException;
import com.stoliar.auth.kafka.UserCreatedProducer;
import com.stoliar.auth.model.Role;
import com.stoliar.auth.repository.AuthUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserCreatedProducer userCreatedProducer;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;

    public AuthService(
            AuthUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            UserCreatedProducer userCreatedProducer,
            EmailVerificationService emailVerificationService,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userCreatedProducer = userCreatedProducer;
        this.emailVerificationService = emailVerificationService;
        this.emailService = emailService;
    }

    @Transactional
    public MessageResponse register(RegisterRequest request) {

        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        Instant now = Instant.now();

        AuthUser user = new AuthUser(
                UUID.randomUUID(),
                email,
                passwordEncoder.encode(request.password()),
                Role.ROLE_USER,
                true,
                false,
                now
        );

        userRepository.save(user);

        String verificationToken =
                emailVerificationService.createToken(user);

        emailService.sendVerificationEmail(
                user,
                verificationToken
        );

        userCreatedProducer.publish(
                user.getId().toString(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );

        return new MessageResponse(
                "Registration successful. Please verify your email."
        );
    }

    @Transactional
    public MessageResponse createUserWithRole(
            CreateUserWithRoleRequest request
    ) {

        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        Instant now = Instant.now();

        AuthUser user = new AuthUser(
                UUID.randomUUID(),
                email,
                passwordEncoder.encode(request.password()),
                request.role(),
                true,
                true,
                now
        );

        userRepository.save(user);

        userCreatedProducer.publish(
                user.getId().toString(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );

        return new MessageResponse(
                "User created successfully with role: "
                        + request.role()
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {

        String email = normalizeEmail(request.email());

        AuthUser user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(
                        InvalidCredentialsException::new
                );

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new InvalidCredentialsException();
        }

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException();
        }

        if (!user.isEmailVerified()) {
            throw new InvalidCredentialsException();
        }

        String accessToken =
                jwtService.generateAccessToken(user);

        String refreshToken =
                refreshTokenService.create(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtService.getAccessTokenTtlSeconds(),
                "Bearer"
        );
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {

        AuthUser user =
                refreshTokenService.validateAndRotate(
                        rawRefreshToken
                );

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException();
        }

        String accessToken =
                jwtService.generateAccessToken(user);

        String newRefreshToken =
                refreshTokenService.create(user);

        return new AuthResponse(
                accessToken,
                newRefreshToken,
                jwtService.getAccessTokenTtlSeconds(),
                "Bearer"
        );
    }

    @Transactional
    public MessageResponse logout(String refreshToken) {

        refreshTokenService.revoke(refreshToken);

        return new MessageResponse(
                "Logged out successfully"
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}