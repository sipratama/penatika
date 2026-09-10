package io.github.sipratama.penatika.identity.adapter.in.security;

import java.util.Objects;

import io.github.sipratama.penatika.identity.application.model.AuthenticatedTeacherSession;

public final class TeacherSessionPrincipal {

    private final AuthenticatedTeacherSession authenticatedSession;

    public TeacherSessionPrincipal(AuthenticatedTeacherSession authenticatedSession) {
        this.authenticatedSession = Objects.requireNonNull(authenticatedSession);
    }

    public AuthenticatedTeacherSession authenticatedSession() {
        return authenticatedSession;
    }

    @Override
    public String toString() {
        return "TeacherSessionPrincipal[REDACTED]";
    }
}
