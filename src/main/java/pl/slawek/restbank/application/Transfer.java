package pl.slawek.restbank.application;

import pl.slawek.restbank.domain.AccountNumber;
import pl.slawek.restbank.common.Money;

public class Transfer {
    private final AccountNumber source;
    private final AccountNumber destination;
    private final Money money;

    public Transfer(AccountNumber source, AccountNumber destination, Money money) {
        this.source = source;
        this.destination = destination;
        this.money = money;
    }

    public AccountNumber source() {
        return source;
    }

    public AccountNumber destination() {
        return destination;
    }

    public Money money() {
        return money;
    }
}
