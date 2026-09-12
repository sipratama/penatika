package io.github.sipratama.penatika.classroom.application.port.in;

import java.util.Optional;

import io.github.sipratama.penatika.classroom.application.model.EstablishedDisplayStream;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;

public interface EstablishClassroomDisplayStreamUseCase {

    EstablishedDisplayStream establish(
            String classroomSessionId,
            Optional<RawSecurityToken> participantCredential);
}
