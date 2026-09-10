package io.github.sipratama.penatika.identity.application.port.out;

import java.time.Instant;
import java.util.Optional;

import io.github.sipratama.penatika.identity.domain.TeacherBrowserSession;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSessionId;

public interface TeacherBrowserSessionPersistencePort {

    void create(TeacherBrowserSession teacherBrowserSession);

    Optional<TeacherBrowserSession> findByCredentialVerifier(String credentialVerifier);

    boolean revoke(TeacherBrowserSessionId id, Instant revokedAt);
}
