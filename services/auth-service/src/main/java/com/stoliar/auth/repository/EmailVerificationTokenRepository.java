package com.stoliar.auth.repository;

import com.stoliar.auth.entity.EmailVerificationToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select token
            from EmailVerificationToken token
            join fetch token.user
            where token.tokenHash = :tokenHash
            """)
    Optional<EmailVerificationToken> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    @Modifying
    @Query("""
            delete from EmailVerificationToken token
            where token.user.id = :userId
            """)
    int deleteByUserId(
            @Param("userId") UUID userId
    );

    @Modifying
    @Query("""
            delete from EmailVerificationToken token
            where token.expiresAt < :now
            """)
    int deleteExpiredTokens(
            @Param("now") Instant now
    );
}