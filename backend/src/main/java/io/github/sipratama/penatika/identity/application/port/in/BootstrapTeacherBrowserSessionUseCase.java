package io.github.sipratama.penatika.identity.application.port.in;

import java.util.Optional;

import io.github.sipratama.penatika.identity.application.model.AuthenticatedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.model.TeacherSessionBootstrap;

public interface BootstrapTeacherBrowserSessionUseCase {

    TeacherSessionBootstrap bootstrap(
            AuthenticatedTeacherSession authenticatedSession,
            Optional<RawSecurityToken> csrfRecoveryToken);
}
