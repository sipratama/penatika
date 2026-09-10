package io.github.sipratama.penatika.identity.application.port.in;

import java.util.Optional;

import io.github.sipratama.penatika.identity.application.model.EstablishedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.ExternalTeacherIdentity;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;

public interface EstablishTeacherBrowserSessionUseCase {

    EstablishedTeacherSession establish(
            ExternalTeacherIdentity externalIdentity,
            Optional<RawSecurityToken> currentBrowserCredential);
}
