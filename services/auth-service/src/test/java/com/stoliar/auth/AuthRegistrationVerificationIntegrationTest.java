package com.stoliar.auth;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.stoliar.auth.kafka.UserCreatedProducer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.mail.internet.MimeMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "admin.enabled=false",
                "mail.enabled=true",
                "mail.from=no-reply@service-orders.local",
                "mail.verification-base-url=http://localhost:8081/api/v1/auth/verify",
                "spring.mail.host=localhost",
                "spring.mail.port=3025",
                "security.jwt.secret=integration-test-secret-key-32-characters-minimum",
                "security.jwt.access-token-ttl=15m",
                "security.jwt.refresh-token-ttl=30d",
                "security.jwt.email-verification-ttl=24h"
        }
)
@AutoConfigureMockMvc
@Testcontainers
class AuthRegistrationVerificationIntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("auth_db")
                    .withUsername("service_orders")
                    .withPassword("service_orders_test");

    private static final GreenMail GREEN_MAIL =
            new GreenMail(ServerSetupTest.SMTP);

    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("token=([A-Za-z0-9_-]+)");

    static {
        POSTGRES.start();
        GREEN_MAIL.start();
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserCreatedProducer userCreatedProducer;

    @BeforeEach
    void clearMailboxes() {
        try {
            GREEN_MAIL.purgeEmailFromAllMailboxes();
        } catch (FolderException e) {
            throw new IllegalStateException(
                    "Failed to clear GreenMail mailboxes before test",
                    e
            );
        }
    }

    @AfterAll
    static void afterAll() {
        GREEN_MAIL.stop();
        POSTGRES.stop();
    }

    @DynamicPropertySource
    static void registerProperties(
            DynamicPropertyRegistry registry
    ) {
        /*
         * These properties are required because application.yml
         * contains ${ADMIN_EMAIL}, ${ADMIN_PASSWORD} and ${ADMIN_ENABLED}.
         */
        registry.add(
                "ADMIN_EMAIL",
                () -> "test-admin@example.com"
        );
        registry.add(
                "ADMIN_PASSWORD",
                () -> "TestAdmin123!"
        );
        registry.add(
                "ADMIN_ENABLED",
                () -> "false"
        );

        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                POSTGRES::getUsername
        );
        registry.add(
                "spring.datasource.password",
                POSTGRES::getPassword
        );

        registry.add(
                "spring.mail.host",
                () -> "localhost"
        );
        registry.add(
                "spring.mail.port",
                () -> ServerSetupTest.SMTP.getPort()
        );
    }

    @Test
    void registerVerificationAndLoginShouldSucceed()
            throws Exception {

        String email = "integration-user@example.com";
        String password = "TestPassword123!";

        String registerRequest = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerRequest)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Registration successful. Please verify your email."
                                )
                );

        GREEN_MAIL.waitForIncomingEmail(
                5000,
                1
        );

        MimeMessage[] messages =
                GREEN_MAIL.getReceivedMessages();

        assertEquals(
                1,
                messages.length,
                "Exactly one verification email should be received"
        );

        assertEquals(
                email,
                messages[0].getAllRecipients()[0].toString()
        );

        assertEquals(
                "Verify your Service Orders email address",
                messages[0].getSubject()
        );

        String body =
                messages[0].getContent().toString();

        assertTrue(
                body.contains(
                        "Please verify your Service Orders account"
                ),
                "Verification email should contain account verification text"
        );

        Matcher matcher =
                TOKEN_PATTERN.matcher(body);

        assertTrue(
                matcher.find(),
                "Verification token was not found in email"
        );

        String verificationToken =
                matcher.group(1);

        String verifyRequest = """
                {
                  "token": "%s"
                }
                """.formatted(verificationToken);

        mockMvc.perform(
                        post("/api/v1/auth/verify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(verifyRequest)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Email successfully verified"
                                )
                );

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "email": "%s",
                                          "password": "%s"
                                        }
                                        """.formatted(
                                                email,
                                                password
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.accessToken")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.refreshToken")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.tokenType")
                                .value("Bearer")
                );
    }

    @Test
    void verificationLinkShouldVerifyEmail()
            throws Exception {

        String email = "link-user@example.com";
        String password = "TestPassword123!";

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "email": "%s",
                                          "password": "%s"
                                        }
                                        """.formatted(
                                                email,
                                                password
                                        )
                                )
                )
                .andExpect(status().isCreated());

        GREEN_MAIL.waitForIncomingEmail(
                5000,
                1
        );

        MimeMessage[] messages =
                GREEN_MAIL.getReceivedMessages();

        assertEquals(
                1,
                messages.length,
                "Exactly one verification email should be received"
        );

        String body =
                messages[0].getContent().toString();

        Matcher matcher =
                TOKEN_PATTERN.matcher(body);

        assertTrue(
                matcher.find(),
                "Verification token was not found in email"
        );

        String verificationToken =
                matcher.group(1);

        mockMvc.perform(
                        get("/api/v1/auth/verify")
                                .param(
                                        "token",
                                        verificationToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Email successfully verified"
                                )
                );

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "email": "%s",
                                          "password": "%s"
                                        }
                                        """.formatted(
                                                email,
                                                password
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.accessToken")
                                .isNotEmpty()
                );
    }

    @Test
    void loginBeforeVerificationShouldReturnUnauthorized()
            throws Exception {

        String email = "unverified-user@example.com";
        String password = "TestPassword123!";

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "email": "%s",
                                          "password": "%s"
                                        }
                                        """.formatted(
                                                email,
                                                password
                                        )
                                )
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "email": "%s",
                                          "password": "%s"
                                        }
                                        """.formatted(
                                                email,
                                                password
                                        )
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid email or password"
                                )
                );
    }
}