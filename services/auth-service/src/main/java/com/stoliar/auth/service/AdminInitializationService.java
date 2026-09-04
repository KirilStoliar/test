package com.stoliar.auth.service;

import com.stoliar.auth.entity.AuthUser;
import com.stoliar.auth.kafka.UserCreatedProducer;
import com.stoliar.auth.model.Role;
import com.stoliar.auth.repository.AuthUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class AdminInitializationService implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(AdminInitializationService.class);

    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCreatedProducer userCreatedProducer;

    private final String adminEmail;
    private final String adminPassword;
    private final boolean adminEnabled;

    public AdminInitializationService(
            AuthUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserCreatedProducer userCreatedProducer,
            @Value("${admin.email}") String adminEmail,
            @Value("${admin.password}") String adminPassword,
            @Value("${admin.enabled:true}") boolean adminEnabled
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userCreatedProducer = userCreatedProducer;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminEnabled = adminEnabled;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!adminEnabled) {
            log.info("Admin auto-creation is disabled");
            return;
        }

        createAdminIfNotExists();
    }

    private void createAdminIfNotExists() {

        String normalizedEmail =
                adminEmail.trim().toLowerCase();

        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            log.info("Admin user already exists: {}", normalizedEmail);
            return;
        }

        Instant now = Instant.now();

        AuthUser admin = new AuthUser(
                UUID.randomUUID(),
                normalizedEmail,
                passwordEncoder.encode(adminPassword),
                Role.ROLE_ADMIN,
                true,
                true,
                now
        );

        userRepository.save(admin);

        userCreatedProducer.publish(
                admin.getId().toString(),
                admin.getEmail(),
                admin.getRole(),
                admin.getCreatedAt()
        );

        log.info(
                "Admin user created successfully: {}",
                normalizedEmail
        );
    }
}