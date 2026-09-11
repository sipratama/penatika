package io.github.sipratama.penatika.classroom.application;

import java.time.Clock;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.UUID;

import io.github.sipratama.penatika.classroom.application.model.AuthorizedController;
import io.github.sipratama.penatika.classroom.application.model.ClassroomCommandRequest;
import io.github.sipratama.penatika.classroom.application.model.ClassroomCommandResult;
import io.github.sipratama.penatika.classroom.application.port.out.AcceptedCommandOutcomePersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.ClassroomSessionPersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.DisplayMutationGatePort;
import io.github.sipratama.penatika.classroom.domain.AcceptedCommandOutcome;
import io.github.sipratama.penatika.classroom.domain.AcceptedCommandOutcomeId;
import io.github.sipratama.penatika.classroom.domain.ClassroomAction;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.classroom.domain.CommandType;
import io.github.sipratama.penatika.classroom.domain.Revision;
import io.github.sipratama.penatika.lesson.application.port.in.ResolveNextLessonSceneUseCase;

public final class ClassroomCommandApplicationService {

    private final ClassroomSessionPersistencePort classroomSessions;
    private final AcceptedCommandOutcomePersistencePort acceptedCommands;
    private final DisplayMutationGatePort displayMutationGate;
    private final ResolveNextLessonSceneUseCase lessonNavigation;
    private final ControllerAuthorityApplicationService controllerAuthority;
    private final Clock clock;

    public ClassroomCommandApplicationService(
            ClassroomSessionPersistencePort classroomSessions,
            AcceptedCommandOutcomePersistencePort acceptedCommands,
            DisplayMutationGatePort displayMutationGate,
            ResolveNextLessonSceneUseCase lessonNavigation,
            ControllerAuthorityApplicationService controllerAuthority,
            Clock clock) {
        this.classroomSessions = Objects.requireNonNull(classroomSessions);
        this.acceptedCommands = Objects.requireNonNull(acceptedCommands);
        this.displayMutationGate = Objects.requireNonNull(displayMutationGate);
        this.lessonNavigation = Objects.requireNonNull(lessonNavigation);
        this.controllerAuthority = Objects.requireNonNull(controllerAuthority);
        this.clock = Objects.requireNonNull(clock);
    }

    public ClassroomCommandResult execute(ClassroomCommandRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        ClassroomSessionId sessionId = new ClassroomSessionId(request.classroomSessionId());
        Revision expectedRevision = new Revision(request.expectedRevision());
        ClassroomSession resolvedSession = classroomSessions.findById(sessionId)
                .orElseThrow(ClassroomSessionNotFoundException::new);
        controllerAuthority.requireAuthority(
                resolvedSession,
                request.teacherAccountId(),
                request.teacherBrowserSessionId(),
                request.participantCredential());

        ClassroomSession session = classroomSessions.lockById(sessionId)
                .orElseThrow(ClassroomSessionNotFoundException::new);
        AuthorizedController controller = controllerAuthority.requireAuthority(
                session,
                request.teacherAccountId(),
                request.teacherBrowserSessionId(),
                request.participantCredential());

        var existing = acceptedCommands.findByCommandIdentity(sessionId, request.commandId());
        if (existing.isPresent()) {
            AcceptedCommandOutcome outcome = existing.orElseThrow();
            if (!outcome.isEquivalentTo(
                    sessionId,
                    request.commandId(),
                    expectedRevision,
                    CommandType.DIRECT_ACTION,
                    ClassroomAction.NEXT)) {
                throw new CommandIdReuseConflictException();
            }
            return result(outcome);
        }
        if (!session.revision().equals(expectedRevision)) {
            throw new StaleRevisionException();
        }
        if (!session.permitsStudentFacingMutation() || session.revision().value() == Revision.MAX_VALUE) {
            throw new ClassroomMutationNotAllowedException();
        }
        OptionalLong nextPosition = lessonNavigation.resolveNextScenePosition(
                session.lessonVersionId(), session.currentScenePosition());
        if (nextPosition.isEmpty()
                || !displayMutationGate.isMutationPermitted(session.id(), session.revision())) {
            throw new ClassroomMutationNotAllowedException();
        }

        ClassroomSession advanced = session.advanceToNextScene(nextPosition.getAsLong());
        if (!classroomSessions.updatePositionIfRevisionMatches(
                session.id(),
                session.revision(),
                advanced.currentScenePosition(),
                advanced.revision(),
                advanced.lifecycleState())) {
            throw new StaleRevisionException();
        }
        AcceptedCommandOutcome outcome = new AcceptedCommandOutcome(
                new AcceptedCommandOutcomeId(UUID.randomUUID()),
                session.id(),
                request.commandId(),
                expectedRevision,
                CommandType.DIRECT_ACTION,
                ClassroomAction.NEXT,
                controller.teacherAccountId(),
                controller.teacherBrowserSessionId(),
                controller.participantSessionId(),
                advanced.revision(),
                clock.instant());
        if (!acceptedCommands.saveIfAbsent(outcome)) {
            throw new UnexpectedAcceptedCommandConflictException();
        }
        return result(outcome);
    }

    private static ClassroomCommandResult result(AcceptedCommandOutcome outcome) {
        return new ClassroomCommandResult(
                outcome.classroomSessionId().value().toString(),
                outcome.commandId(),
                outcome.resultingRevision().value());
    }
}
