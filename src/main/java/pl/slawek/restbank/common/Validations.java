package pl.slawek.restbank.common;

public class Validations {
    public static <V> V requireNotNull(V v, String name) {
        if (v == null) {
            throw new RequireNotNullParameterException(name);
        }
        return v;
    }
}
