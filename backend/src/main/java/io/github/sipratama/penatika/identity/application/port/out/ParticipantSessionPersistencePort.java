package io.github.sipratama.penatika.identity.application.port.out;

import java.time.Instant;
import java.util.Optional;

import io.github.sipratama.penatika.identity.domain.ParticipantSession;
import io.github.sipratama.penatika.identity.domain.ParticipantSessionId;
import io.github.sipratama.penatika.identity.domain.ClassroomSessionReference;
import io.github.sipratama.penatika.identity.domain.ParticipantRole;

public interface ParticipantSessionPersistencePort {

    boolean tryCreateActive(ParticipantSession participantSession);

    Optional<ParticipantSession> findByCredentialVerifier(String credentialVerifier);

    Optional<ParticipantSession> findActiveByRole(
            ClassroomSessionReference classroomSessionId,
            ParticipantRole participantRole);

    int revokeInvalidLifetimeOccupants(
            ClassroomSessionReference classroomSessionId,
            ParticipantRole participantRole,
            Instant now);

    boolean revoke(ParticipantSessionId id, Instant revokedAt);
}
