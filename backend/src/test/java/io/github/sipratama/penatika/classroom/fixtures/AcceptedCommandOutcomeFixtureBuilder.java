package io.github.sipratama.penatika.classroom.fixtures;

import java.time.Instant;
import java.util.UUID;

import io.github.sipratama.penatika.classroom.domain.AcceptedCommandOutcome;
import io.github.sipratama.penatika.classroom.domain.AcceptedCommandOutcomeId;
import io.github.sipratama.penatika.classroom.domain.ClassroomAction;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.classroom.domain.CommandType;
import io.github.sipratama.penatika.classroom.domain.Revision;

public final class AcceptedCommandOutcomeFixtureBuilder {

    private AcceptedCommandOutcomeId id =
            new AcceptedCommandOutcomeId(UUID.fromString("30000000-0000-0000-0000-000000000003"));
    private ClassroomSessionId classroomSessionId =
            new ClassroomSessionId(UUID.fromString("30000000-0000-0000-0000-000000000001"));
    private String commandId = "command-001";
    private UUID teacherAccountId = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private UUID teacherBrowserSessionId = UUID.fromString("10000000-0000-0000-0000-000000000003");
    private UUID participantSessionId = UUID.fromString("10000000-0000-0000-0000-000000000004");

    public AcceptedCommandOutcomeFixtureBuilder withId(UUID id) {
        this.id = new AcceptedCommandOutcomeId(id);
        return this;
    }

    public AcceptedCommandOutcomeFixtureBuilder withCommandId(String commandId) {
        this.commandId = commandId;
        return this;
    }

    public AcceptedCommandOutcomeFixtureBuilder withContext(
            UUID classroomSessionId,
            UUID teacherAccountId,
            UUID teacherBrowserSessionId,
            UUID participantSessionId) {
        this.classroomSessionId = new ClassroomSessionId(classroomSessionId);
        this.teacherAccountId = teacherAccountId;
        this.teacherBrowserSessionId = teacherBrowserSessionId;
        this.participantSessionId = participantSessionId;
        return this;
    }

    public AcceptedCommandOutcome build() {
        return new AcceptedCommandOutcome(
                id,
                classroomSessionId,
                commandId,
                new Revision(0),
                CommandType.DIRECT_ACTION,
                ClassroomAction.NEXT,
                teacherAccountId,
                teacherBrowserSessionId,
                participantSessionId,
                new Revision(1),
                Instant.parse("2026-01-01T10:07:00Z"));
    }
}
