package pl.slawek.restbank.common;

public abstract class ProgrammaticException extends RuntimeException {
    public ProgrammaticException() {
        super("Internal Error");
    }

    public ProgrammaticException(String message) {
        super(message);
    }
}
