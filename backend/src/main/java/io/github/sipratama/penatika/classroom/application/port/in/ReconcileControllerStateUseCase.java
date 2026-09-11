package io.github.sipratama.penatika.classroom.application.port.in;

import java.util.Optional;
import java.util.UUID;

import io.github.sipratama.penatika.classroom.application.model.ControllerReconciliationState;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;

public interface ReconcileControllerStateUseCase {

    ControllerReconciliationState reconcile(
            UUID classroomSessionId,
            UUID teacherAccountId,
            UUID teacherBrowserSessionId,
            Optional<RawSecurityToken> participantCredential);
}
