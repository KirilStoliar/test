package com.stoliar.auth.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "email_verification_tokens",
        indexes = {
                @Index(
                        name = "idx_email_verification_token_hash",
                        columnList = "token_hash"
                ),
                @Index(
                        name = "idx_email_verification_token_user",
                        columnList = "user_id"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_email_verification_token_user",
                        columnNames = "user_id"
                )
        }
)
public class EmailVerificationToken {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_email_verification_user"
            )
    )
    private AuthUser user;

    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 44
    )
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected EmailVerificationToken() {
    }

    public EmailVerificationToken(
            UUID id,
            AuthUser user,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt
    ) {
        this.id = id;
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public AuthUser getUser() {
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isExpired() {
        return !expiresAt.isAfter(Instant.now());
    }
}