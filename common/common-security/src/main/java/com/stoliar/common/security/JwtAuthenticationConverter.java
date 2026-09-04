package com.stoliar.common.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class JwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        Collection<SimpleGrantedAuthority> authorities =
                extractAuthorities(jwt);

        return new JwtAuthenticationToken(
                jwt,
                authorities,
                jwt.getSubject()
        );
    }

    private Collection<SimpleGrantedAuthority> extractAuthorities(
            Jwt jwt
    ) {

        Object rolesClaim = jwt.getClaim(ROLES_CLAIM);

        if (rolesClaim == null) {
            return List.of();
        }

        if (rolesClaim instanceof Collection<?> roles) {
            return roles.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(this::normalizeRole)
                    .filter(role -> !role.isBlank())
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }

        if (rolesClaim instanceof String role) {

            String normalized = normalizeRole(role);

            if (normalized.isBlank()) {
                return List.of();
            }

            return List.of(
                    new SimpleGrantedAuthority(normalized)
            );
        }

        return List.of();
    }

    private String normalizeRole(String role) {

        if (role == null) {
            return "";
        }

        String normalized = role.trim();

        if (normalized.isEmpty()) {
            return "";
        }

        if (normalized.startsWith(ROLE_PREFIX)) {
            return normalized;
        }

        return ROLE_PREFIX + normalized;
    }
}