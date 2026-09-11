package io.github.sipratama.penatika.classroom.application.port.in;

import java.util.UUID;

import io.github.sipratama.penatika.classroom.application.model.IssuedPairingGrant;

public interface IssuePairingGrantUseCase {

    IssuedPairingGrant issue(String classroomSessionId, String participantRole, UUID teacherAccountId);
}
