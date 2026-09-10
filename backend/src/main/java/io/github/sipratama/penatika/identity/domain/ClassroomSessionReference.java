package io.github.sipratama.penatika.identity.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Opaque reference to a Classroom Session owned by the classroom module.
 * Identity never depends on classroom's domain or adapter packages; this
 * value type only carries the physical identifier used for database
 * foreign-key integrity.
 */
public record ClassroomSessionReference(UUID value) {

    public ClassroomSessionReference {
        Objects.requireNonNull(value, "value must not be null");
    }
}
