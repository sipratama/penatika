package io.github.sipratama.penatika.identity.application.port.in;

import io.github.sipratama.penatika.identity.application.model.AuthorizedTeacherSession;

public interface RecordTeacherSessionActivityUseCase {

    void recordActivity(AuthorizedTeacherSession authorizedSession);
}
