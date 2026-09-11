package io.github.sipratama.penatika.classroom.application;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import io.github.sipratama.penatika.classroom.application.model.ControllerReconciliationState;
import io.github.sipratama.penatika.classroom.application.port.in.ReconcileControllerStateUseCase;
import io.github.sipratama.penatika.classroom.application.port.out.ClassroomSessionPersistencePort;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;

public final class ControllerReconciliationApplicationService
        implements ReconcileControllerStateUseCase {

    private final ClassroomSessionPersistencePort classroomSessions;
    private final ControllerAuthorityApplicationService controllerAuthority;

    public ControllerReconciliationApplicationService(
            ClassroomSessionPersistencePort classroomSessions,
            ControllerAuthorityApplicationService controllerAuthority) {
        this.classroomSessions = Objects.requireNonNull(classroomSessions);
        this.controllerAuthority = Objects.requireNonNull(controllerAuthority);
    }

    @Override
    public ControllerReconciliationState reconcile(
            UUID classroomSessionId,
            UUID teacherAccountId,
            UUID teacherBrowserSessionId,
            Optional<RawSecurityToken> participantCredential) {
        ClassroomSession session = classroomSessions.findById(new ClassroomSessionId(classroomSessionId))
                .orElseThrow(ClassroomSessionNotFoundException::new);
        controllerAuthority.requireAuthority(
                session, teacherAccountId, teacherBrowserSessionId, participantCredential);
        return new ControllerReconciliationState(
                session.id().value().toString(), session.revision().value());
    }
}
