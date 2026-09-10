package io.github.sipratama.penatika.identity.fixtures;

import java.time.Instant;
import java.util.UUID;

import io.github.sipratama.penatika.identity.domain.ClassroomSessionReference;
import io.github.sipratama.penatika.identity.domain.ParticipantRole;
import io.github.sipratama.penatika.identity.domain.ParticipantSession;
import io.github.sipratama.penatika.identity.domain.ParticipantSessionId;
import io.github.sipratama.penatika.identity.domain.TeacherAccountId;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSessionId;

public final class ParticipantSessionFixtureBuilder {

    private ParticipantSessionId id =
            new ParticipantSessionId(UUID.fromString("10000000-0000-0000-0000-000000000004"));
    private ClassroomSessionReference classroomSessionId =
            new ClassroomSessionReference(UUID.fromString("30000000-0000-0000-0000-000000000001"));
    private ParticipantRole role = ParticipantRole.CLASSROOM_DISPLAY;
    private String credentialVerifier = "3".repeat(64);
    private TeacherAccountId teacherAccountId;
    private TeacherBrowserSessionId teacherBrowserSessionId;
    private Instant createdAt = Instant.parse("2026-01-01T10:05:00Z");

    public ParticipantSessionFixtureBuilder withId(UUID id) {
        this.id = new ParticipantSessionId(id);
        return this;
    }

    public ParticipantSessionFixtureBuilder withClassroomSessionId(UUID classroomSessionId) {
        this.classroomSessionId = new ClassroomSessionReference(classroomSessionId);
        return this;
    }

    public ParticipantSessionFixtureBuilder withVerifier(String verifier) {
        this.credentialVerifier = verifier;
        return this;
    }

    public ParticipantSessionFixtureBuilder asController(
            TeacherAccountId teacherAccountId, TeacherBrowserSessionId teacherBrowserSessionId) {
        this.role = ParticipantRole.TEACHER_CONTROLLER;
        this.teacherAccountId = teacherAccountId;
        this.teacherBrowserSessionId = teacherBrowserSessionId;
        return this;
    }

    public ParticipantSession build() {
        return new ParticipantSession(
                id,
                classroomSessionId,
                role,
                credentialVerifier,
                teacherAccountId,
                teacherBrowserSessionId,
                createdAt,
                null,
                null);
    }
}
