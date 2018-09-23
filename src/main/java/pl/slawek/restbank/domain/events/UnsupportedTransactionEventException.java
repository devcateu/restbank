package pl.slawek.restbank.domain.events;

import pl.slawek.restbank.common.ProgrammaticException;

public class UnsupportedTransactionEventException extends ProgrammaticException {
    public UnsupportedTransactionEventException() {
        super();
    }
}
