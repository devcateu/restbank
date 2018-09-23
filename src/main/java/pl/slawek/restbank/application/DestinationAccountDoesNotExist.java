package pl.slawek.restbank.application;

import pl.slawek.restbank.common.BusinessException;

public class DestinationAccountDoesNotExist extends BusinessException {
    public DestinationAccountDoesNotExist() {
        super("Destination Account does not exist");
    }
}
