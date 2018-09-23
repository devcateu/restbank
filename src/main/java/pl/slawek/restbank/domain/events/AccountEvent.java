package pl.slawek.restbank.domain.events;

import pl.slawek.restbank.domain.AccountNumber;

import java.time.ZonedDateTime;

public abstract class AccountEvent {
    private final AccountNumber accountNumber;
    private final ZonedDateTime occurred;

    public AccountEvent(AccountNumber accountNumber) {
        this.accountNumber = accountNumber;
        this.occurred = ZonedDateTime.now();
    }

    public AccountNumber accountNumber() {
        return accountNumber;
    }

    public ZonedDateTime occurred() {
        return occurred;
    }
}
