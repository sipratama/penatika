package io.github.sipratama.penatika.identity.domain;

import java.util.Objects;
import java.util.UUID;

public record ParticipantSessionId(UUID value) {

    public ParticipantSessionId {
        Objects.requireNonNull(value, "value must not be null");
    }
}
