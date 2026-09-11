package io.github.sipratama.penatika.classroom.adapter.out.synchronization;

import io.github.sipratama.penatika.classroom.application.port.out.DisplayMutationGatePort;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.classroom.domain.Revision;

public final class FailClosedDisplayMutationGate implements DisplayMutationGatePort {

    @Override
    public boolean isMutationPermitted(
            ClassroomSessionId classroomSessionId,
            Revision currentRevision) {
        return false;
    }
}
