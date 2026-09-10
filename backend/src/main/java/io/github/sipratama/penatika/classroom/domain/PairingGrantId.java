package io.github.sipratama.penatika.classroom.domain;

import java.util.Objects;
import java.util.UUID;

public record PairingGrantId(UUID value) {

    public PairingGrantId {
        Objects.requireNonNull(value, "value must not be null");
    }
}
