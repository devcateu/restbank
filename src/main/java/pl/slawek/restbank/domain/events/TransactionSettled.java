package pl.slawek.restbank.domain.events;

import pl.slawek.restbank.domain.AccountNumber;
import pl.slawek.restbank.common.Money;
import pl.slawek.restbank.domain.TransactionId;

public class TransactionSettled extends TransactionalEvent {
    private final Money amount;

    public TransactionSettled(AccountNumber accountNumber, TransactionId transactionId, Money amount) {
        super(accountNumber, transactionId);
        this.amount = amount;
    }

    public Money amount() {
        return amount;
    }
}
