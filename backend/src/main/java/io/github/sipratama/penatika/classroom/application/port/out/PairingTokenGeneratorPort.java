package io.github.sipratama.penatika.classroom.application.port.out;

import io.github.sipratama.penatika.classroom.application.model.RawPairingToken;

public interface PairingTokenGeneratorPort {

    RawPairingToken generate();
}
