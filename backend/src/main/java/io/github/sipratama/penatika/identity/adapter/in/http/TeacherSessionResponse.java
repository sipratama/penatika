package io.github.sipratama.penatika.identity.adapter.in.http;

import java.util.Objects;

public final class TeacherSessionResponse {

    private final String csrfToken;

    public TeacherSessionResponse(String csrfToken) {
        this.csrfToken = Objects.requireNonNull(csrfToken, "csrfToken must not be null");
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    @Override
    public String toString() {
        return "TeacherSessionResponse[REDACTED]";
    }
}
