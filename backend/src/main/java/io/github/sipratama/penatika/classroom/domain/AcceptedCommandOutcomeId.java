package io.github.sipratama.penatika.classroom.domain;

import java.util.Objects;
import java.util.UUID;

public record AcceptedCommandOutcomeId(UUID value) {

    public AcceptedCommandOutcomeId {
        Objects.requireNonNull(value, "value must not be null");
    }
}
