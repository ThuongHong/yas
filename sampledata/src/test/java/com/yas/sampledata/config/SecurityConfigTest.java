package com.yas.sampledata.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void jwtAuthenticationConverterForKeycloak_shouldMapRolesToAuthorities() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverterForKeycloak();
        Jwt jwt = jwtWithClaims(Map.of("realm_access", Map.of("roles", List.of("ADMIN", "STAFF"))));

        Collection<? extends GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        assertThat(authorities)
            .extracting(GrantedAuthority::getAuthority)
            .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_STAFF");
    }

    @Test
    void jwtAuthenticationConverterForKeycloak_shouldReturnEmptyAuthoritiesWhenRealmAccessMissing() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverterForKeycloak();
        Jwt jwt = jwtWithClaims(Map.of("sub", "123"));

        Collection<? extends GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        assertThat(authorities).isEmpty();
    }

    @Test
    void jwtAuthenticationConverterForKeycloak_shouldReturnEmptyAuthoritiesWhenRolesMissing() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverterForKeycloak();
        Jwt jwt = jwtWithClaims(Map.of("realm_access", Map.of()));

        Collection<? extends GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        assertThat(authorities).isEmpty();
    }

    private Jwt jwtWithClaims(Map<String, Object> claims) {
        return new Jwt(
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(600),
            Map.of("alg", "none"),
            claims
        );
    }
}
