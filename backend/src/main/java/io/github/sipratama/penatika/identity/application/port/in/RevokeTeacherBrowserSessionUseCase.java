package io.github.sipratama.penatika.identity.application.port.in;

import io.github.sipratama.penatika.identity.domain.TeacherBrowserSessionId;

public interface RevokeTeacherBrowserSessionUseCase {

    boolean revoke(TeacherBrowserSessionId sessionId);
}
