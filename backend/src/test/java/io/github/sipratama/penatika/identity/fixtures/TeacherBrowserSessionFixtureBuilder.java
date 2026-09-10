package io.github.sipratama.penatika.identity.fixtures;

import java.time.Instant;
import java.util.UUID;

import io.github.sipratama.penatika.identity.domain.TeacherAccountId;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSession;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSessionId;

public final class TeacherBrowserSessionFixtureBuilder {

    private TeacherBrowserSessionId id =
            new TeacherBrowserSessionId(UUID.fromString("10000000-0000-0000-0000-000000000003"));
    private TeacherAccountId teacherAccountId =
            new TeacherAccountId(UUID.fromString("10000000-0000-0000-0000-000000000001"));
    private String credentialVerifier = "1".repeat(64);
    private String csrfVerifier = "2".repeat(64);
    private Instant createdAt = Instant.parse("2026-01-01T10:01:00Z");
    private Instant lastActiveAt = Instant.parse("2026-01-01T10:02:00Z");
    private Instant idleExpiresAt = Instant.parse("2026-01-01T11:02:00Z");
    private Instant absoluteExpiresAt = Instant.parse("2026-01-02T10:01:00Z");

    public TeacherBrowserSessionFixtureBuilder withId(UUID id) {
        this.id = new TeacherBrowserSessionId(id);
        return this;
    }

    public TeacherBrowserSessionFixtureBuilder withTeacherAccountId(TeacherAccountId teacherAccountId) {
        this.teacherAccountId = teacherAccountId;
        return this;
    }

    public TeacherBrowserSessionFixtureBuilder withVerifiers(
            String credentialVerifier, String csrfVerifier) {
        this.credentialVerifier = credentialVerifier;
        this.csrfVerifier = csrfVerifier;
        return this;
    }

    public TeacherBrowserSession build() {
        return new TeacherBrowserSession(
                id,
                teacherAccountId,
                credentialVerifier,
                csrfVerifier,
                createdAt,
                lastActiveAt,
                idleExpiresAt,
                absoluteExpiresAt,
                null);
    }
}
