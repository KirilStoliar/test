package com.stoliar.auth.service;

import com.stoliar.auth.entity.AuthUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    private static final String ISSUER =
            "service-orders-auth";

    private final JwtEncoder jwtEncoder;

    private final Duration accessTokenTtl;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.access-token-ttl:15m}")
            Duration accessTokenTtl
    ) {
        this.jwtEncoder = jwtEncoder;
        this.accessTokenTtl = accessTokenTtl;
    }

    public String generateAccessToken(
            AuthUser user
    ) {

        Instant issuedAt = Instant.now();

        Instant expiresAt =
                issuedAt.plus(accessTokenTtl);

        JwtClaimsSet claims =
                JwtClaimsSet.builder()

                        .issuer(ISSUER)

                        .issuedAt(issuedAt)

                        .expiresAt(expiresAt)

                        .subject(
                                user.getId().toString()
                        )

                        .claim(
                                "email",
                                user.getEmail()
                        )

                        .claim(
                                "roles",
                                List.of(
                                        user.getRole().name()
                                )
                        )

                        .build();

        JwsHeader header =
                JwsHeader.with(
                        MacAlgorithm.HS256
                ).build();

        return jwtEncoder
                .encode(
                        JwtEncoderParameters.from(
                                header,
                                claims
                        )
                )
                .getTokenValue();
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtl.toSeconds();
    }
}