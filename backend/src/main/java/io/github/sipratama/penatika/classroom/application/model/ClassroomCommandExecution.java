package io.github.sipratama.penatika.classroom.application.model;

import java.util.Objects;

/**
 * Internal execution disposition returned by {@code ClassroomCommandApplicationService},
 * distinguishing a newly accepted mutation from an equivalent accepted-outcome replay so the
 * transaction boundary can decide whether to trigger a post-commit Display projection
 * dispatch. This is never exposed through the public wire-facing command result.
 */
public record ClassroomCommandExecution(ClassroomCommandResult result, boolean newlyAccepted) {

    public ClassroomCommandExecution {
        Objects.requireNonNull(result, "result must not be null");
    }
}
