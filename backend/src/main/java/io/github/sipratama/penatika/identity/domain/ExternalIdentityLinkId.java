package io.github.sipratama.penatika.identity.domain;

import java.util.Objects;
import java.util.UUID;

public record ExternalIdentityLinkId(UUID value) {

    public ExternalIdentityLinkId {
        Objects.requireNonNull(value, "value must not be null");
    }
}
