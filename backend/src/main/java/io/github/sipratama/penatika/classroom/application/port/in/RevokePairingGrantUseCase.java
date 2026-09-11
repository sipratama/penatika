package io.github.sipratama.penatika.classroom.application.port.in;

import java.util.UUID;

public interface RevokePairingGrantUseCase {

    void revoke(String classroomSessionId, String pairingGrantId, UUID teacherAccountId);
}
