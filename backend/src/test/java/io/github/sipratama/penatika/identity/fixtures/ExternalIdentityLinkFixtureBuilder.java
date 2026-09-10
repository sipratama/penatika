package io.github.sipratama.penatika.identity.fixtures;

import java.time.Instant;
import java.util.UUID;

import io.github.sipratama.penatika.identity.domain.ExternalIdentityLink;
import io.github.sipratama.penatika.identity.domain.ExternalIdentityLinkId;
import io.github.sipratama.penatika.identity.domain.TeacherAccountId;

public final class ExternalIdentityLinkFixtureBuilder {

    private ExternalIdentityLinkId id =
            new ExternalIdentityLinkId(UUID.fromString("10000000-0000-0000-0000-000000000002"));
    private TeacherAccountId teacherAccountId =
            new TeacherAccountId(UUID.fromString("10000000-0000-0000-0000-000000000001"));
    private String issuer = "https://identity.test.example";
    private String subject = "teacher-subject-001";
    private Instant createdAt = Instant.parse("2026-01-01T10:00:01Z");

    public ExternalIdentityLinkFixtureBuilder withId(UUID id) {
        this.id = new ExternalIdentityLinkId(id);
        return this;
    }

    public ExternalIdentityLinkFixtureBuilder withTeacherAccountId(TeacherAccountId teacherAccountId) {
        this.teacherAccountId = teacherAccountId;
        return this;
    }

    public ExternalIdentityLinkFixtureBuilder withIdentity(String issuer, String subject) {
        this.issuer = issuer;
        this.subject = subject;
        return this;
    }

    public ExternalIdentityLink build() {
        return new ExternalIdentityLink(id, teacherAccountId, issuer, subject, createdAt);
    }
}
