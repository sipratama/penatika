package io.github.sipratama.penatika.identity.application.port.in;

import io.github.sipratama.penatika.identity.application.model.AuthenticatedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.AuthorizedTeacherSession;

public interface AuthorizeTeacherReadUseCase {

    AuthorizedTeacherSession authorizeRead(AuthenticatedTeacherSession authenticatedSession);
}
