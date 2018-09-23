package pl.slawek.restbank.common;

public abstract class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
