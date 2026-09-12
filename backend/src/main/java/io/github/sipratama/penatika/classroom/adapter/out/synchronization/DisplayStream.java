package io.github.sipratama.penatika.classroom.adapter.out.synchronization;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * One process-local Display SSE stream generation. All outbound writes (projection sends and
 * heartbeats) are serialized through this instance so a heartbeat and a post-commit projection
 * dispatch can never write to the same emitter concurrently. Never persisted; lost on restart
 * by design.
 */
public final class DisplayStream {

    private final UUID classroomSessionId;
    private final UUID participantSessionId;
    private final long generation;
    private final SseEmitter emitter;
    private final Object writeLock = new Object();

    private volatile Instant lastSuccessfulWriteAt;
    private volatile Long lastDispatchedProjectionRevision;
    private volatile Long acknowledgedRevision;
    private volatile boolean terminated;

    public DisplayStream(
            UUID classroomSessionId,
            UUID participantSessionId,
            long generation,
            SseEmitter emitter,
            Instant establishedAt) {
        this.classroomSessionId = Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        this.participantSessionId = Objects.requireNonNull(participantSessionId, "participantSessionId must not be null");
        this.generation = generation;
        this.emitter = Objects.requireNonNull(emitter, "emitter must not be null");
        this.lastSuccessfulWriteAt = Objects.requireNonNull(establishedAt, "establishedAt must not be null");
    }

    public UUID classroomSessionId() {
        return classroomSessionId;
    }

    public UUID participantSessionId() {
        return participantSessionId;
    }

    public long generation() {
        return generation;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public Instant lastSuccessfulWriteAt() {
        return lastSuccessfulWriteAt;
    }

    public Long lastDispatchedProjectionRevision() {
        return lastDispatchedProjectionRevision;
    }

    public Long acknowledgedRevision() {
        return acknowledgedRevision;
    }

    public boolean sendProjection(long revision, Object data, Instant now) {
        synchronized (writeLock) {
            if (terminated) {
                return false;
            }
            try {
                emitter.send(SseEmitter.event()
                        .id(Long.toString(revision))
                        .name("display-projection")
                        .data(data, MediaType.APPLICATION_JSON));
            } catch (IOException | IllegalStateException exception) {
                return false;
            }
            lastSuccessfulWriteAt = now;
            lastDispatchedProjectionRevision = revision;
            acknowledgedRevision = null;
            return true;
        }
    }

    public boolean sendHeartbeat(Instant now) {
        synchronized (writeLock) {
            if (terminated) {
                return false;
            }
            try {
                emitter.send(SseEmitter.event().comment("keep-alive"));
            } catch (IOException | IllegalStateException exception) {
                return false;
            }
            lastSuccessfulWriteAt = now;
            return true;
        }
    }

    public boolean acknowledge(long revision) {
        synchronized (writeLock) {
            if (terminated) {
                return false;
            }
            Long dispatched = lastDispatchedProjectionRevision;
            if (dispatched == null || dispatched.longValue() != revision) {
                return false;
            }
            acknowledgedRevision = revision;
            return true;
        }
    }

    public void terminate() {
        synchronized (writeLock) {
            if (terminated) {
                return;
            }
            terminated = true;
            try {
                emitter.complete();
            } catch (RuntimeException ignored) {
                // best-effort completion; the stream is already considered invalid.
            }
        }
    }
}
