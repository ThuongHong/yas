package com.yas.storefrontbff.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig(mock(ReactiveClientRegistrationRepository.class));

    @Test
    void generateAuthoritiesFromClaim_shouldPrefixRoles() {
        Collection<GrantedAuthority> authorities = securityConfig.generateAuthoritiesFromClaim(List.of("USER", "ADMIN"));

        assertThat(authorities)
            .extracting(GrantedAuthority::getAuthority)
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void userAuthoritiesMapperForKeycloak_shouldMapOidcRealmRoles() {
        GrantedAuthoritiesMapper mapper = securityConfig.userAuthoritiesMapperForKeycloak();
        OidcUserInfo userInfo = mock(OidcUserInfo.class);
        OidcUserAuthority oidcUserAuthority = mock(OidcUserAuthority.class);
        when(oidcUserAuthority.getUserInfo()).thenReturn(userInfo);
        when(userInfo.hasClaim("realm_access")).thenReturn(true);
        when(userInfo.getClaimAsMap("realm_access")).thenReturn(Map.of("roles", List.of("USER")));

        Collection<? extends GrantedAuthority> mappedAuthorities = mapper.mapAuthorities(List.of(oidcUserAuthority));

        assertThat(mappedAuthorities)
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_USER");
    }

    @Test
    void userAuthoritiesMapperForKeycloak_shouldMapOAuth2RealmRoles() {
        GrantedAuthoritiesMapper mapper = securityConfig.userAuthoritiesMapperForKeycloak();
        OAuth2UserAuthority authority = new OAuth2UserAuthority(
            Map.of("realm_access", Map.of("roles", List.of("CUSTOMER")))
        );

        Collection<? extends GrantedAuthority> mappedAuthorities = mapper.mapAuthorities(List.of(authority));

        assertThat(mappedAuthorities)
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_CUSTOMER");
    }

    @Test
    void userAuthoritiesMapperForKeycloak_shouldReturnEmptyWhenClaimMissing() {
        GrantedAuthoritiesMapper mapper = securityConfig.userAuthoritiesMapperForKeycloak();
        OAuth2UserAuthority authority = new OAuth2UserAuthority(Map.of("sub", "1"));

        Collection<? extends GrantedAuthority> mappedAuthorities = mapper.mapAuthorities(List.of(authority));

        assertThat(mappedAuthorities).isEmpty();
    }

    @Test
    void userAuthoritiesMapperForKeycloak_shouldThrowWhenRolesMissing() {
        GrantedAuthoritiesMapper mapper = securityConfig.userAuthoritiesMapperForKeycloak();
        OAuth2UserAuthority authority = new OAuth2UserAuthority(Map.of("realm_access", Map.of()));

        assertThatThrownBy(() -> mapper.mapAuthorities(List.of(authority)))
            .isInstanceOf(NullPointerException.class);
    }
}
