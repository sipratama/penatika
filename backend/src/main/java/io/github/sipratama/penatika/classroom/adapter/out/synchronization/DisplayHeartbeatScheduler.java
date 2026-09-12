package io.github.sipratama.penatika.classroom.adapter.out.synchronization;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import io.github.sipratama.penatika.identity.application.port.in.ParticipantSessionAuthorityUseCase;

/**
 * One bounded, testable liveness sweep shared by all current Display streams. Production
 * wiring schedules {@link #sweep()} periodically on a single bounded executor; tests call it
 * directly with a fake Clock, never relying on real wall-clock sleeps.
 *
 * <p>For each current stream: revalidate effective Display authority; if the stream has been
 * dead for at least the configured dead timeout, invalidate it; otherwise, if it is due for a
 * heartbeat, attempt one comment-only send and invalidate on failure.
 */
public final class DisplayHeartbeatScheduler {

    private final DisplayStreamRegistry registry;
    private final ParticipantSessionAuthorityUseCase participantSessions;
    private final Clock clock;
    private final Duration heartbeatInterval;
    private final Duration deadTimeout;

    public DisplayHeartbeatScheduler(
            DisplayStreamRegistry registry,
            ParticipantSessionAuthorityUseCase participantSessions,
            Clock clock,
            Duration heartbeatInterval,
            Duration deadTimeout) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.participantSessions = Objects.requireNonNull(participantSessions, "participantSessions must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.heartbeatInterval = Objects.requireNonNull(heartbeatInterval, "heartbeatInterval must not be null");
        this.deadTimeout = Objects.requireNonNull(deadTimeout, "deadTimeout must not be null");
    }

    public void sweep() {
        for (DisplayStream stream : registry.allCurrent()) {
            sweepOne(stream);
        }
    }

    private void sweepOne(DisplayStream stream) {
        if (stream.isTerminated()) {
            registry.unregisterIfCurrent(stream.classroomSessionId(), stream);
            return;
        }
        if (!participantSessions.hasEffectiveDisplay(stream.classroomSessionId(), stream.participantSessionId())) {
            invalidate(stream);
            return;
        }
        Instant now = clock.instant();
        Duration age = Duration.between(stream.lastSuccessfulWriteAt(), now);
        if (age.compareTo(deadTimeout) >= 0) {
            invalidate(stream);
            return;
        }
        if (age.compareTo(heartbeatInterval) >= 0) {
            if (!stream.sendHeartbeat(now)) {
                invalidate(stream);
            }
        }
    }

    private void invalidate(DisplayStream stream) {
        registry.unregisterIfCurrent(stream.classroomSessionId(), stream);
        stream.terminate();
    }
}
