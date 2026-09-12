package io.github.sipratama.penatika.classroom.adapter.out.synchronization;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Process-local registry of the current Display SSE stream generation per ClassroomSessionId.
 * Only one generation is ever current for a given ClassroomSessionId. Replacing the current
 * generation best-effort terminates the previous one, but never removes the newly registered
 * generation; cleanup callbacks bound to a superseded generation therefore cannot invalidate a
 * newer one. Backend restart starts with an empty registry by design; nothing here is persisted.
 */
public final class DisplayStreamRegistry {

    private final Map<UUID, DisplayStream> currentStreams = new ConcurrentHashMap<>();
    private final AtomicLong generationSequence = new AtomicLong();

    public DisplayStream register(
            UUID classroomSessionId,
            UUID participantSessionId,
            SseEmitter emitter,
            Instant now) {
        long generation = generationSequence.incrementAndGet();
        DisplayStream stream = new DisplayStream(classroomSessionId, participantSessionId, generation, emitter, now);
        DisplayStream previous = currentStreams.put(classroomSessionId, stream);
        if (previous != null) {
            previous.terminate();
        }
        return stream;
    }

    public Optional<DisplayStream> current(UUID classroomSessionId) {
        return Optional.ofNullable(currentStreams.get(classroomSessionId));
    }

    public void unregisterIfCurrent(UUID classroomSessionId, DisplayStream stream) {
        currentStreams.remove(classroomSessionId, stream);
    }

    public List<DisplayStream> allCurrent() {
        return List.copyOf(currentStreams.values());
    }
}
