package io.github.sipratama.penatika.classroom.application.port.out;

import java.util.UUID;

import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.classroom.domain.Revision;

/**
 * Process-local Display synchronization acknowledgement gateway. Accepts an acknowledgement
 * only when the requested revision matches both the current durable Classroom revision and
 * the exact revision most recently dispatched on the requesting participant's current live
 * stream generation.
 */
public interface DisplaySynchronizationAcknowledgementPort {

    boolean acknowledge(
            ClassroomSessionId classroomSessionId,
            UUID participantSessionId,
            Revision requestedRevision,
            Revision currentDurableRevision);
}
