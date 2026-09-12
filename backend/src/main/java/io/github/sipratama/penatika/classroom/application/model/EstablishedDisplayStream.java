package io.github.sipratama.penatika.classroom.application.model;

import java.util.Objects;
import java.util.UUID;

import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;

/**
 * Framework-free authorization result for establishing a Display SSE stream: the canonical
 * ClassroomSessionId, the resolved Display participantSessionId, and the current full
 * projection to send as the first state-bearing event. Carries no SseEmitter or other
 * transport type.
 */
public record EstablishedDisplayStream(
        ClassroomSessionId classroomSessionId,
        UUID participantSessionId,
        ClassroomDisplayProjection projection) {

    public EstablishedDisplayStream {
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(participantSessionId, "participantSessionId must not be null");
        Objects.requireNonNull(projection, "projection must not be null");
    }
}
