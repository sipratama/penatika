package io.github.sipratama.penatika.classroom.application.port.in;

import java.util.Optional;

import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;

public interface AcknowledgeDisplaySynchronizationUseCase {

    void acknowledge(
            String classroomSessionId,
            Optional<RawSecurityToken> participantCredential,
            boolean validDisplayIntent,
            long revision);
}
