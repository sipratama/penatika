package io.github.sipratama.penatika.classroom.fixtures;

import java.time.Instant;
import java.util.UUID;

import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.classroom.domain.PairingGrant;
import io.github.sipratama.penatika.classroom.domain.PairingGrantId;
import io.github.sipratama.penatika.classroom.domain.PairingRole;

public final class PairingGrantFixtureBuilder {

    private PairingGrantId id =
            new PairingGrantId(UUID.fromString("30000000-0000-0000-0000-000000000002"));
    private ClassroomSessionId classroomSessionId =
            new ClassroomSessionId(UUID.fromString("30000000-0000-0000-0000-000000000001"));
    private PairingRole role = PairingRole.CLASSROOM_DISPLAY;
    private String verifier = "4".repeat(64);
    private Instant issuedAt = Instant.parse("2026-01-01T10:06:00Z");

    public PairingGrantFixtureBuilder withId(UUID id) {
        this.id = new PairingGrantId(id);
        return this;
    }

    public PairingGrantFixtureBuilder withClassroomSessionId(UUID classroomSessionId) {
        this.classroomSessionId = new ClassroomSessionId(classroomSessionId);
        return this;
    }

    public PairingGrantFixtureBuilder withVerifier(String verifier) {
        this.verifier = verifier;
        return this;
    }

    public PairingGrant build() {
        return new PairingGrant(
                id,
                classroomSessionId,
                role,
                verifier,
                issuedAt,
                issuedAt.plusSeconds(300),
                null,
                null);
    }
}
