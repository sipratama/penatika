package io.github.sipratama.penatika.identity.adapter.in.security;

import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public final class TeacherSessionAuthentication extends AbstractAuthenticationToken {

    private final TeacherSessionPrincipal principal;

    public TeacherSessionAuthentication(TeacherSessionPrincipal principal) {
        super(List.of());
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public TeacherSessionPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return "";
    }
}
