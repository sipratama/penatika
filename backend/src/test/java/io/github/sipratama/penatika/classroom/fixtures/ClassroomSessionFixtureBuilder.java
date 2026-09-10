package io.github.sipratama.penatika.classroom.fixtures;

import java.time.Instant;
import java.util.UUID;

import io.github.sipratama.penatika.classroom.domain.ClassroomLifecycleState;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.classroom.domain.Revision;

public final class ClassroomSessionFixtureBuilder {

    private ClassroomSessionId id =
            new ClassroomSessionId(UUID.fromString("30000000-0000-0000-0000-000000000001"));
    private UUID teacherAccountId = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private UUID lessonVersionId = UUID.fromString("20000000-0000-0000-0000-000000000002");

    public ClassroomSessionFixtureBuilder withId(UUID id) {
        this.id = new ClassroomSessionId(id);
        return this;
    }

    public ClassroomSessionFixtureBuilder withReferences(UUID teacherAccountId, UUID lessonVersionId) {
        this.teacherAccountId = teacherAccountId;
        this.lessonVersionId = lessonVersionId;
        return this;
    }

    public ClassroomSession build() {
        return new ClassroomSession(
                id,
                teacherAccountId,
                lessonVersionId,
                ClassroomLifecycleState.READY,
                0,
                new Revision(0),
                Instant.parse("2026-01-01T10:05:00Z"));
    }
}
