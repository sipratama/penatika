package io.github.sipratama.penatika.identity.domain;

import java.time.Instant;
import java.util.Objects;

public record ExternalIdentityLink(
        ExternalIdentityLinkId id,
        TeacherAccountId teacherAccountId,
        String issuer,
        String subject,
        Instant createdAt) {

    public ExternalIdentityLink {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(teacherAccountId, "teacherAccountId must not be null");
        Objects.requireNonNull(issuer, "issuer must not be null");
        Objects.requireNonNull(subject, "subject must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        if (issuer.isBlank()) {
            throw new IllegalArgumentException("issuer must not be blank");
        }
        if (subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank");
        }
    }
}
