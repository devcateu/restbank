package pl.slawek.restbank.domain.events;

import pl.slawek.restbank.common.ProgrammaticException;

public class UnsupportedAccountEventException extends ProgrammaticException {
    public UnsupportedAccountEventException() {
        super();
    }
}
