package com.example.nba.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Translates a Keycloak JWT into Spring authorities.
 *
 * <p>Supports three claim shapes so the central RBAC team can choose their mapping:
 * <ul>
 *   <li>{@code permissions}: flat array of permission strings (preferred)</li>
 *   <li>{@code realm_access.roles}: realm roles</li>
 *   <li>{@code resource_access.<client>.roles}: client roles</li>
 * </ul>
 * All are added verbatim as authorities, so a Keycloak role named {@code PROGRAM_CREATE}
 * and a permission {@code PROGRAM_CREATE} both satisfy {@code hasAuthority('PROGRAM_CREATE')}.
 */
public class KeycloakJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    @SuppressWarnings("unchecked")
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

        // 1. flat "permissions" claim
        Object perms = jwt.getClaim("permissions");
        if (perms instanceof Collection<?> c) {
            c.forEach(p -> authorities.add(new SimpleGrantedAuthority(String.valueOf(p))));
        }

        // 2. realm_access.roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
            roles.forEach(r -> authorities.add(new SimpleGrantedAuthority(String.valueOf(r))));
        }

        // 3. resource_access.<client>.roles
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            for (Object client : resourceAccess.values()) {
                if (client instanceof Map<?, ?> m && m.get("roles") instanceof Collection<?> roles) {
                    roles.forEach(r -> authorities.add(new SimpleGrantedAuthority(String.valueOf(r))));
                }
            }
        }

        String principal = jwt.getClaimAsString("preferred_username");
        if (principal == null) {
            principal = jwt.getSubject();
        }
        return new JwtAuthenticationToken(jwt, List.copyOf(authorities), principal);
    }
}
