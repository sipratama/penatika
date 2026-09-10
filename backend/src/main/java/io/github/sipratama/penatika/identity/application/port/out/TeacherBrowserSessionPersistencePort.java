package io.github.sipratama.penatika.identity.application.port.out;

import java.time.Instant;
import java.util.Optional;

import io.github.sipratama.penatika.identity.domain.TeacherBrowserSession;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSessionId;

public interface TeacherBrowserSessionPersistencePort {

    void create(TeacherBrowserSession teacherBrowserSession);

    void createReplacing(
            TeacherBrowserSession teacherBrowserSession,
            Optional<String> replacedCredentialVerifier,
            Instant replacedAt);

    Optional<TeacherBrowserSession> findByCredentialVerifier(String credentialVerifier);

    Optional<TeacherBrowserSession> findById(TeacherBrowserSessionId id);

    boolean refreshActivity(
            TeacherBrowserSessionId id,
            Instant activityAt,
            Instant idleExpiresAt);

    boolean replaceCsrfVerifierAndRefreshActivity(
            TeacherBrowserSessionId id,
            String expectedCsrfVerifier,
            String replacementCsrfVerifier,
            Instant activityAt,
            Instant idleExpiresAt);

    boolean revoke(TeacherBrowserSessionId id, Instant revokedAt);
}
