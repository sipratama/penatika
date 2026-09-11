package io.github.sipratama.penatika.classroom.application.port.in;

import io.github.sipratama.penatika.classroom.application.model.EstablishedParticipant;
import io.github.sipratama.penatika.classroom.application.model.PresentedPairingToken;

public interface EstablishClassroomDisplayParticipantUseCase {

    EstablishedParticipant establishDisplay(PresentedPairingToken pairingToken);
}
