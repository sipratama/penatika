package io.github.sipratama.penatika.identity.adapter.in.security;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.sipratama.penatika.identity.application.port.in.AuthenticateTeacherBrowserSessionUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class TeacherSessionAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticateTeacherBrowserSessionUseCase authenticateSession;
    private final TeacherSessionCookies cookies;

    public TeacherSessionAuthenticationFilter(
            AuthenticateTeacherBrowserSessionUseCase authenticateSession,
            TeacherSessionCookies cookies) {
        this.authenticateSession = authenticateSession;
        this.cookies = cookies;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
        cookies.readSessionCredential(request)
                .flatMap(authenticateSession::authenticate)
                .ifPresent(authenticatedSession -> {
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(new TeacherSessionAuthentication(
                            new TeacherSessionPrincipal(authenticatedSession)));
                    SecurityContextHolder.setContext(context);
                });
        filterChain.doFilter(request, response);
    }
}
