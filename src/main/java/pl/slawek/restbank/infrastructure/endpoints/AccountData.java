package pl.slawek.restbank.infrastructure.endpoints;

import pl.slawek.restbank.domain.Account;

import java.math.BigDecimal;

public class AccountData {

    private final String accountNumber;
    private final BigDecimal balance;
    private final String owner;

    public AccountData(Account account) {
        accountNumber = account.accountNumber().toString();
        balance = account.balance().amount();
        owner = account.ownerId().toString();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getOwner() {
        return owner;
    }
}
