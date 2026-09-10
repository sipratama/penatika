package io.github.sipratama.penatika.identity.application.port.out;

import java.util.Optional;

import io.github.sipratama.penatika.identity.domain.ExternalIdentityLink;
import io.github.sipratama.penatika.identity.domain.TeacherAccount;
import io.github.sipratama.penatika.identity.domain.TeacherAccountId;

public interface TeacherIdentityPersistencePort {

    void create(TeacherAccount teacherAccount, ExternalIdentityLink externalIdentityLink);

    Optional<ExternalIdentityLink> findExternalIdentity(String issuer, String subject);

    Optional<TeacherAccount> findTeacherAccount(TeacherAccountId teacherAccountId);
}
