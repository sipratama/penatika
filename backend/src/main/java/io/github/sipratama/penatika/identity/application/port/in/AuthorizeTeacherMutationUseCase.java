package io.github.sipratama.penatika.identity.application.port.in;

import java.util.Optional;

import io.github.sipratama.penatika.identity.application.model.AuthenticatedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.AuthorizedTeacherMutation;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;

public interface AuthorizeTeacherMutationUseCase {

    AuthorizedTeacherMutation authorizeMutation(
            AuthenticatedTeacherSession authenticatedSession,
            Optional<RawSecurityToken> presentedToken);
}
