package io.github.sipratama.penatika.classroom.application.port.out;

import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;

/**
 * Publishes the current authoritative Display projection to the process-local current SSE
 * stream generation for a ClassroomSession, if any. Implementations must run outside any
 * database transaction and must be safe to invoke even when no stream is currently active.
 */
public interface DisplayProjectionPublisherPort {

    void publishCurrentProjection(ClassroomSessionId classroomSessionId);
}
