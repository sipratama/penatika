package io.github.sipratama.penatika.classroom.application.port.out;

import io.github.sipratama.penatika.classroom.application.model.PresentedPairingToken;
import io.github.sipratama.penatika.classroom.application.model.RawPairingToken;

public interface PairingTokenVerifierPort {

    String verifierFor(RawPairingToken token);

    String verifierFor(PresentedPairingToken token);
}
