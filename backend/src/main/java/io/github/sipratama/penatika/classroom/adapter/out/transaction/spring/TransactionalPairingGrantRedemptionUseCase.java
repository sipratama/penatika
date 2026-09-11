package io.github.sipratama.penatika.classroom.adapter.out.transaction.spring;

import java.util.Objects;
import java.util.UUID;

import org.springframework.transaction.support.TransactionTemplate;

import io.github.sipratama.penatika.classroom.application.PairingGrantRedemptionApplicationService;
import io.github.sipratama.penatika.classroom.application.model.EstablishedParticipant;
import io.github.sipratama.penatika.classroom.application.model.PresentedPairingToken;
import io.github.sipratama.penatika.classroom.application.port.in.EstablishClassroomDisplayParticipantUseCase;
import io.github.sipratama.penatika.classroom.application.port.in.EstablishTeacherControllerParticipantUseCase;

public final class TransactionalPairingGrantRedemptionUseCase
        implements EstablishTeacherControllerParticipantUseCase,
        EstablishClassroomDisplayParticipantUseCase {

    private final PairingGrantRedemptionApplicationService delegate;
    private final TransactionTemplate transactionTemplate;

    public TransactionalPairingGrantRedemptionUseCase(
            PairingGrantRedemptionApplicationService delegate,
            TransactionTemplate transactionTemplate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.transactionTemplate = Objects.requireNonNull(transactionTemplate, "transactionTemplate must not be null");
    }

    @Override
    public EstablishedParticipant establishController(
            PresentedPairingToken pairingToken,
            UUID teacherAccountId,
            UUID teacherBrowserSessionId) {
        return Objects.requireNonNull(transactionTemplate.execute(status -> delegate.establishController(
                pairingToken, teacherAccountId, teacherBrowserSessionId)));
    }

    @Override
    public EstablishedParticipant establishDisplay(PresentedPairingToken pairingToken) {
        return Objects.requireNonNull(transactionTemplate.execute(
                status -> delegate.establishDisplay(pairingToken)));
    }
}
