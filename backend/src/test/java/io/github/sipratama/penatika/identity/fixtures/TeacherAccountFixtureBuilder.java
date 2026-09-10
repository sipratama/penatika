package io.github.sipratama.penatika.identity.fixtures;

import java.time.Instant;
import java.util.UUID;

import io.github.sipratama.penatika.identity.domain.TeacherAccount;
import io.github.sipratama.penatika.identity.domain.TeacherAccountId;
import io.github.sipratama.penatika.identity.domain.TeacherAccountStatus;

public final class TeacherAccountFixtureBuilder {

    private TeacherAccountId id = new TeacherAccountId(UUID.fromString("10000000-0000-0000-0000-000000000001"));
    private TeacherAccountStatus status = TeacherAccountStatus.ACTIVE;
    private Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");

    public TeacherAccountFixtureBuilder withId(UUID id) {
        this.id = new TeacherAccountId(id);
        return this;
    }

    public TeacherAccountFixtureBuilder withStatus(TeacherAccountStatus status) {
        this.status = status;
        return this;
    }

    public TeacherAccount build() {
        return new TeacherAccount(id, status, createdAt);
    }
}
