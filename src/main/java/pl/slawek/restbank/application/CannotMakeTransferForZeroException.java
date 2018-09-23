package pl.slawek.restbank.application;

import pl.slawek.restbank.common.ValidationException;

public class CannotMakeTransferForZeroException extends ValidationException {
    public CannotMakeTransferForZeroException() {
        super("Cannot make transfer for zero");
    }
}
