package pl.slawek.restbank.common;

public class AmountCannotBeBelowZero extends ValidationException {
    public AmountCannotBeBelowZero() {
        super("Amount cannot be below zero");
    }
}
