package pl.slawek.restbank.domain.events;

import pl.slawek.restbank.domain.AccountNumber;
import pl.slawek.restbank.common.Money;
import pl.slawek.restbank.domain.TransactionId;

public class TransactionRejected extends TransactionalEvent {
    private final Money amount;

    public TransactionRejected(AccountNumber transactionOwner, TransactionId transactionId, Money amount) {
        super(transactionOwner, transactionId);
        this.amount = amount;
    }

    public Money amount() {
        return amount;
    }
}
