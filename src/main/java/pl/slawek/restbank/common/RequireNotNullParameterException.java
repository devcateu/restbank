package pl.slawek.restbank.common;

public class RequireNotNullParameterException extends ValidationException {
    public RequireNotNullParameterException(String name) {
        super("Required parameter is empty: " + name);
    }
}
