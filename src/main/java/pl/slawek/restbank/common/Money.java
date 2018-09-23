package pl.slawek.restbank.common;

import java.math.BigDecimal;
import java.util.Objects;

public class Money {
    private BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = Validations.requireNotNull(amount, "amount");
        if (this.amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new AmountCannotBeBelowZero();
        }
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money of(long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    public Money add(Money other) {
        Validations.requireNotNull(other, "amount");
        final BigDecimal otherAmount = other.amount;
        return new Money(this.amount.add(otherAmount));
    }

    public Money minus(Money other) {
        Validations.requireNotNull(other, "amount");
        final BigDecimal otherAmount = other.amount;
        return new Money(this.amount.add(otherAmount.negate()));
    }

    public boolean isGreaterThan(Money balance) {
        return amount.compareTo(balance.amount) > 0;
    }

    public BigDecimal amount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return amount.toString();
    }
}
