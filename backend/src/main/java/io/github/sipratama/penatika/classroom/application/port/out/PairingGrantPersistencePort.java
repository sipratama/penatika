package io.github.sipratama.penatika.classroom.application.port.out;

import java.time.Instant;
import java.util.Optional;

import io.github.sipratama.penatika.classroom.domain.PairingGrant;
import io.github.sipratama.penatika.classroom.domain.PairingGrantId;

public interface PairingGrantPersistencePort {

    void create(PairingGrant pairingGrant);

    Optional<PairingGrant> findByCredentialVerifier(String credentialVerifier);

    Optional<PairingGrant> consumeByCredentialVerifier(String credentialVerifier, Instant consumedAt);

    boolean revoke(PairingGrantId pairingGrantId, Instant revokedAt);
}
