package pl.slawek.restbank.domain;

import pl.slawek.restbank.common.Validations;

import java.util.Objects;

public final class AccountNumber {
    private final String id;

    private AccountNumber(String id) {
        this.id = Validations.requireNotNull(id, "accountNumber");
    }

    public static AccountNumber of(String accountNumber) {
        return new AccountNumber(accountNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountNumber that = (AccountNumber) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
