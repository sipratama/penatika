package io.github.sipratama.penatika.classroom.application;

public final class PairingGrantRejectedException extends RuntimeException {

    public PairingGrantRejectedException() {
        super("The PairingGrant cannot authorize participant establishment");
    }
}
