package io.github.sipratama.penatika.identity.application.model;

import java.util.Objects;

public record ExternalTeacherIdentity(String issuer, String subject) {

    public ExternalTeacherIdentity {
        Objects.requireNonNull(issuer, "issuer must not be null");
        Objects.requireNonNull(subject, "subject must not be null");
        if (issuer.isBlank() || subject.isBlank()) {
            throw new IllegalArgumentException("validated external identity must not be blank");
        }
    }
}
