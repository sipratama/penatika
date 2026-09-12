package io.github.sipratama.penatika.classroom.adapter.out.synchronization;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

import io.github.sipratama.penatika.classroom.application.ClassroomDisplayProjectionApplicationService;
import io.github.sipratama.penatika.classroom.application.model.ClassroomDisplayProjection;
import io.github.sipratama.penatika.classroom.application.port.out.ClassroomSessionPersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.DisplayProjectionPublisherPort;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;

/**
 * Publishes the current authoritative Display projection to the registry's current stream
 * generation for a ClassroomSession, strictly after the owning command transaction has already
 * committed. A missing or already-terminated current stream is a no-op: an accepted durable
 * mutation is never rolled back because no Display is currently reachable.
 */
public final class ClassroomDisplayProjectionPublisher implements DisplayProjectionPublisherPort {

    private final DisplayStreamRegistry registry;
    private final ClassroomSessionPersistencePort classroomSessions;
    private final ClassroomDisplayProjectionApplicationService projections;
    private final Clock clock;

    public ClassroomDisplayProjectionPublisher(
            DisplayStreamRegistry registry,
            ClassroomSessionPersistencePort classroomSessions,
            ClassroomDisplayProjectionApplicationService projections,
            Clock clock) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.classroomSessions = Objects.requireNonNull(classroomSessions, "classroomSessions must not be null");
        this.projections = Objects.requireNonNull(projections, "projections must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void publishCurrentProjection(ClassroomSessionId classroomSessionId) {
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        DisplayStream stream = registry.current(classroomSessionId.value()).orElse(null);
        if (stream == null || stream.isTerminated()) {
            return;
        }
        ClassroomSession session = classroomSessions.findById(classroomSessionId).orElse(null);
        if (session == null) {
            invalidate(classroomSessionId.value(), stream);
            return;
        }
        ClassroomDisplayProjection projection = projections.deriveProjection(session);
        boolean sent = stream.sendProjection(
                projection.revision(), new DisplayProjectionWireModel(projection), clock.instant());
        if (!sent) {
            invalidate(classroomSessionId.value(), stream);
        }
    }

    private void invalidate(UUID classroomSessionId, DisplayStream stream) {
        registry.unregisterIfCurrent(classroomSessionId, stream);
        stream.terminate();
    }
}
