package io.github.sipratama.penatika.identity.application.port.in;

import java.util.Optional;

import io.github.sipratama.penatika.identity.application.model.AuthenticatedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;

public interface AuthenticateTeacherBrowserSessionUseCase {

    Optional<AuthenticatedTeacherSession> authenticate(RawSecurityToken credential);
}
