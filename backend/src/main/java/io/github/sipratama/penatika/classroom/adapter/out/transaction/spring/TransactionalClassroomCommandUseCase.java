package io.github.sipratama.penatika.classroom.adapter.out.transaction.spring;

import java.util.Objects;

import org.springframework.transaction.support.TransactionTemplate;

import io.github.sipratama.penatika.classroom.application.ClassroomCommandApplicationService;
import io.github.sipratama.penatika.classroom.application.UnexpectedAcceptedCommandConflictException;
import io.github.sipratama.penatika.classroom.application.model.ClassroomCommandRequest;
import io.github.sipratama.penatika.classroom.application.model.ClassroomCommandResult;
import io.github.sipratama.penatika.classroom.application.port.in.ExecuteClassroomCommandUseCase;

public final class TransactionalClassroomCommandUseCase implements ExecuteClassroomCommandUseCase {

    private final ClassroomCommandApplicationService delegate;
    private final TransactionTemplate transactionTemplate;

    public TransactionalClassroomCommandUseCase(
            ClassroomCommandApplicationService delegate,
            TransactionTemplate transactionTemplate) {
        this.delegate = Objects.requireNonNull(delegate);
        this.transactionTemplate = Objects.requireNonNull(transactionTemplate);
    }

    @Override
    public ClassroomCommandResult execute(ClassroomCommandRequest request) {
        try {
            return inTransaction(request);
        } catch (UnexpectedAcceptedCommandConflictException conflict) {
            return inTransaction(request);
        }
    }

    private ClassroomCommandResult inTransaction(ClassroomCommandRequest request) {
        return Objects.requireNonNull(
                transactionTemplate.execute(status -> delegate.execute(request)));
    }
}
