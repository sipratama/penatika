package io.github.sipratama.penatika.classroom.application.port.out;

import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.classroom.domain.Revision;

public interface DisplayMutationGatePort {

    boolean isMutationPermitted(ClassroomSessionId classroomSessionId, Revision currentRevision);
}
