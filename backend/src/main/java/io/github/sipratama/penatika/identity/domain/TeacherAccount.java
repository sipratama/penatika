package io.github.sipratama.penatika.identity.domain;

import java.time.Instant;
import java.util.Objects;

public record TeacherAccount(TeacherAccountId id, TeacherAccountStatus status, Instant createdAt) {

    public TeacherAccount {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public boolean isActive() {
        return status == TeacherAccountStatus.ACTIVE;
    }
}
