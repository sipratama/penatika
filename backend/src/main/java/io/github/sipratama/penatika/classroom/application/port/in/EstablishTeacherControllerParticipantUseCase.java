package io.github.sipratama.penatika.classroom.application.port.in;

import java.util.UUID;

import io.github.sipratama.penatika.classroom.application.model.EstablishedParticipant;
import io.github.sipratama.penatika.classroom.application.model.PresentedPairingToken;

public interface EstablishTeacherControllerParticipantUseCase {

    EstablishedParticipant establishController(
            PresentedPairingToken pairingToken,
            UUID teacherAccountId,
            UUID teacherBrowserSessionId);
}
