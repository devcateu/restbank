package pl.slawek.restbank.common;

public abstract class BusinessException extends Exception {

    public BusinessException(String message) {
        super(message);
    }
}
