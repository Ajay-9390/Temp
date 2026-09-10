package com.example.nba.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Local-dev only. Injects a mock "dev-user" authenticated with every permission from
 * {@link PermissionCatalog}, so the module is fully usable without a Keycloak server while
 * still exercising the {@code @PreAuthorize} annotations. Never active when
 * {@code app.security.enabled=true}.
 */
public class DevAuthenticationFilter extends OncePerRequestFilter {

    private static final List<GrantedAuthority> ALL_AUTHORITIES = loadAllPermissions();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            var auth = new UsernamePasswordAuthenticationToken("dev-user", "n/a", ALL_AUTHORITIES);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    private static List<GrantedAuthority> loadAllPermissions() {
        List<GrantedAuthority> list = new ArrayList<>();
        for (Field f : PermissionCatalog.class.getDeclaredFields()) {
            // Only the raw permission constants (not the HAS_* SpEL helpers).
            if (Modifier.isStatic(f.getModifiers()) && f.getType() == String.class
                    && !f.getName().startsWith("HAS_")) {
                try {
                    list.add(new SimpleGrantedAuthority((String) f.get(null)));
                } catch (IllegalAccessException ignored) {
                    // constant is public; won't happen
                }
            }
        }
        return list;
    }
}
