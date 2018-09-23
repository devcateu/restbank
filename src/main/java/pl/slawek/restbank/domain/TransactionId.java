package pl.slawek.restbank.domain;

import pl.slawek.restbank.common.Validations;

import java.util.Objects;

public final class TransactionId {
    private final String id;

    private TransactionId(String id) {
        this.id = Validations.requireNotNull(id, "transactionId");
    }

    public static TransactionId of(String transactionId) {
        return new TransactionId(transactionId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionId that = (TransactionId) o;
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
