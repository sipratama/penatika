package io.github.sipratama.penatika.classroom.application.port.in;

import java.util.Optional;

import io.github.sipratama.penatika.classroom.application.model.ClassroomDisplayProjection;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;

public interface GetClassroomDisplaySnapshotUseCase {

    ClassroomDisplayProjection getSnapshot(
            String classroomSessionId,
            Optional<RawSecurityToken> participantCredential);
}
