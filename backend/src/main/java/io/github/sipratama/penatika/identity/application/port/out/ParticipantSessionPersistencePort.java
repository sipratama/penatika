package io.github.sipratama.penatika.identity.application.port.out;

import java.time.Instant;
import java.util.Optional;

import io.github.sipratama.penatika.identity.domain.ParticipantSession;
import io.github.sipratama.penatika.identity.domain.ParticipantSessionId;

public interface ParticipantSessionPersistencePort {

    boolean tryCreateActive(ParticipantSession participantSession);

    Optional<ParticipantSession> findByCredentialVerifier(String credentialVerifier);

    boolean revoke(ParticipantSessionId id, Instant revokedAt);
}
