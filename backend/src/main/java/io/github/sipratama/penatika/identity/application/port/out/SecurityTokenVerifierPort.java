package io.github.sipratama.penatika.identity.application.port.out;

import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;

public interface SecurityTokenVerifierPort {

    String verifierFor(RawSecurityToken token);

    boolean matches(RawSecurityToken token, String verifier);
}
