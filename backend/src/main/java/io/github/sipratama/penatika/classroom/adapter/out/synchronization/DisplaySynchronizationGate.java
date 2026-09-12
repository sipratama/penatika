package io.github.sipratama.penatika.classroom.adapter.out.synchronization;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import io.github.sipratama.penatika.classroom.application.port.out.DisplayMutationGatePort;
import io.github.sipratama.penatika.classroom.application.port.out.DisplaySynchronizationAcknowledgementPort;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.classroom.domain.Revision;
import io.github.sipratama.penatika.identity.application.port.in.ParticipantSessionAuthorityUseCase;

/**
 * The real process-local Display mutation gate and synchronization acknowledgement gateway,
 * replacing the production fail-closed placeholder. A mutation is permitted only while the
 * current stream generation belongs to a still-effective Display participant, is not
 * terminated, has liveness age below the dead timeout, and has both dispatched and
 * acknowledged the exact current revision.
 */
public final class DisplaySynchronizationGate
        implements DisplayMutationGatePort, DisplaySynchronizationAcknowledgementPort {

    private final DisplayStreamRegistry registry;
    private final ParticipantSessionAuthorityUseCase participantSessions;
    private final Clock clock;
    private final Duration deadTimeout;

    public DisplaySynchronizationGate(
            DisplayStreamRegistry registry,
            ParticipantSessionAuthorityUseCase participantSessions,
            Clock clock,
            Duration deadTimeout) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.participantSessions = Objects.requireNonNull(participantSessions, "participantSessions must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.deadTimeout = Objects.requireNonNull(deadTimeout, "deadTimeout must not be null");
    }

    @Override
    public boolean isMutationPermitted(ClassroomSessionId classroomSessionId, Revision currentRevision) {
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(currentRevision, "currentRevision must not be null");
        DisplayStream stream = registry.current(classroomSessionId.value()).orElse(null);
        if (stream == null || stream.isTerminated()) {
            return false;
        }
        if (!isLive(stream, clock.instant())) {
            return false;
        }
        Long dispatched = stream.lastDispatchedProjectionRevision();
        Long acknowledged = stream.acknowledgedRevision();
        if (dispatched == null || acknowledged == null) {
            return false;
        }
        if (dispatched.longValue() != currentRevision.value() || acknowledged.longValue() != currentRevision.value()) {
            return false;
        }
        return participantSessions.hasEffectiveDisplay(
                classroomSessionId.value(), stream.participantSessionId());
    }

    @Override
    public boolean acknowledge(
            ClassroomSessionId classroomSessionId,
            UUID participantSessionId,
            Revision requestedRevision,
            Revision currentDurableRevision) {
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(participantSessionId, "participantSessionId must not be null");
        Objects.requireNonNull(requestedRevision, "requestedRevision must not be null");
        Objects.requireNonNull(currentDurableRevision, "currentDurableRevision must not be null");
        if (!requestedRevision.equals(currentDurableRevision)) {
            return false;
        }
        DisplayStream stream = registry.current(classroomSessionId.value()).orElse(null);
        if (stream == null || stream.isTerminated()) {
            return false;
        }
        if (!stream.participantSessionId().equals(participantSessionId)) {
            return false;
        }
        Instant now = clock.instant();
        if (!isLive(stream, now)) {
            invalidate(classroomSessionId.value(), stream);
            return false;
        }
        Long dispatched = stream.lastDispatchedProjectionRevision();
        if (dispatched == null || dispatched.longValue() != currentDurableRevision.value()) {
            return false;
        }
        return stream.acknowledge(currentDurableRevision.value());
    }

    private boolean isLive(DisplayStream stream, Instant now) {
        Duration age = Duration.between(stream.lastSuccessfulWriteAt(), now);
        return age.compareTo(deadTimeout) < 0;
    }

    private void invalidate(UUID classroomSessionId, DisplayStream stream) {
        registry.unregisterIfCurrent(classroomSessionId, stream);
        stream.terminate();
    }
}
