package com.stoliar.product.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfiguration {

    private static final String ALGORITHM = "HmacSHA256";
    private static final String ISSUER = "service-orders-auth";

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${security.jwt.secret}") String secret
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "security.jwt.secret must not be blank"
            );
        }

        byte[] keyBytes =
                secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "security.jwt.secret must contain at least 32 bytes"
            );
        }

        return new SecretKeySpec(
                keyBytes,
                ALGORITHM
        );
    }

    @Bean
    public JwtEncoder jwtEncoder(
            SecretKey jwtSecretKey
    ) {
        return new NimbusJwtEncoder(
                new ImmutableSecret<>(jwtSecretKey)
        );
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey
    ) {
        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withSecretKey(jwtSecretKey)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        ISSUER
                )
        );

        return decoder;
    }
}