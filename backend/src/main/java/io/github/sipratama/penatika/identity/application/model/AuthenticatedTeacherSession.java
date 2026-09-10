package io.github.sipratama.penatika.identity.application.model;

import java.util.Objects;

import io.github.sipratama.penatika.identity.domain.TeacherAccount;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSession;

public record AuthenticatedTeacherSession(TeacherAccount teacherAccount, TeacherBrowserSession session) {

    public AuthenticatedTeacherSession {
        Objects.requireNonNull(teacherAccount, "teacherAccount must not be null");
        Objects.requireNonNull(session, "session must not be null");
    }

    @Override
    public String toString() {
        return "AuthenticatedTeacherSession[REDACTED]";
    }
}
