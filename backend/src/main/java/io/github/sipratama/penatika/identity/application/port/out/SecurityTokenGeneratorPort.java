package io.github.sipratama.penatika.identity.application.port.out;

import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;

public interface SecurityTokenGeneratorPort {

    RawSecurityToken generate();
}
