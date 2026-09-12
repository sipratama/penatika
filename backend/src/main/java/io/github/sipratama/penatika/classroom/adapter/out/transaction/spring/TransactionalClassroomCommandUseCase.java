package io.github.sipratama.penatika.classroom.adapter.out.transaction.spring;

import java.util.Objects;
import java.util.UUID;

import org.springframework.transaction.support.TransactionTemplate;

import io.github.sipratama.penatika.classroom.application.ClassroomCommandApplicationService;
import io.github.sipratama.penatika.classroom.application.UnexpectedAcceptedCommandConflictException;
import io.github.sipratama.penatika.classroom.application.model.ClassroomCommandExecution;
import io.github.sipratama.penatika.classroom.application.model.ClassroomCommandRequest;
import io.github.sipratama.penatika.classroom.application.model.ClassroomCommandResult;
import io.github.sipratama.penatika.classroom.application.port.in.ExecuteClassroomCommandUseCase;
import io.github.sipratama.penatika.classroom.application.port.out.DisplayProjectionPublisherPort;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;

public final class TransactionalClassroomCommandUseCase implements ExecuteClassroomCommandUseCase {

    private final ClassroomCommandApplicationService delegate;
    private final TransactionTemplate transactionTemplate;
    private final DisplayProjectionPublisherPort projectionPublisher;

    public TransactionalClassroomCommandUseCase(
            ClassroomCommandApplicationService delegate,
            TransactionTemplate transactionTemplate,
            DisplayProjectionPublisherPort projectionPublisher) {
        this.delegate = Objects.requireNonNull(delegate);
        this.transactionTemplate = Objects.requireNonNull(transactionTemplate);
        this.projectionPublisher = Objects.requireNonNull(projectionPublisher);
    }

    @Override
    public ClassroomCommandResult execute(ClassroomCommandRequest request) {
        ClassroomCommandExecution execution;
        try {
            execution = inTransaction(request);
        } catch (UnexpectedAcceptedCommandConflictException conflict) {
            execution = inTransaction(request);
        }
        if (execution.newlyAccepted()) {
            projectionPublisher.publishCurrentProjection(
                    new ClassroomSessionId(UUID.fromString(execution.result().classroomSessionId())));
        }
        return execution.result();
    }

    private ClassroomCommandExecution inTransaction(ClassroomCommandRequest request) {
        return Objects.requireNonNull(
                transactionTemplate.execute(status -> delegate.execute(request)));
    }
}
