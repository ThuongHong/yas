package com.yas.sampledata.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void jwtAuthenticationConverterForKeycloak_shouldMapRolesToAuthorities() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverterForKeycloak();
        Jwt jwt = jwtWithClaims(Map.of("realm_access", Map.of("roles", List.of("ADMIN", "STAFF"))));

        Collection<GrantedAuthority> authorities =
            (Collection<GrantedAuthority>) jwtGrantedAuthoritiesConverter(converter).convert(jwt);

        assertThat(authorities)
            .extracting(GrantedAuthority::getAuthority)
            .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_STAFF");
    }

    @Test
    void jwtAuthenticationConverterForKeycloak_shouldThrowWhenRealmAccessMissing() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverterForKeycloak();
        Jwt jwt = jwtWithClaims(Map.of("sub", "123"));

        assertThatThrownBy(() -> jwtGrantedAuthoritiesConverter(converter).convert(jwt))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void jwtAuthenticationConverterForKeycloak_shouldThrowWhenRolesMissing() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverterForKeycloak();
        Jwt jwt = jwtWithClaims(Map.of("realm_access", Map.of()));

        assertThatThrownBy(() -> jwtGrantedAuthoritiesConverter(converter).convert(jwt))
            .isInstanceOf(NullPointerException.class);
    }

    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter(
        JwtAuthenticationConverter converter
    ) {
        try {
            Field field = JwtAuthenticationConverter.class.getDeclaredField("jwtGrantedAuthoritiesConverter");
            field.setAccessible(true);
            return (Converter<Jwt, Collection<GrantedAuthority>>) field.get(converter);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to access jwtGrantedAuthoritiesConverter", e);
        }
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
