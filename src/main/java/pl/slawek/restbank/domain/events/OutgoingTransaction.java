package pl.slawek.restbank.domain.events;

import pl.slawek.restbank.common.Money;
import pl.slawek.restbank.domain.*;

public class OutgoingTransaction extends TransactionalEvent {
    private final AccountNumber destination;
    private final Money amount;

    public OutgoingTransaction(AccountNumber owner, TransactionId transactionId, AccountNumber destination, Money amount) {
        super(owner, transactionId);
        this.destination = destination;
        this.amount = amount;
    }

    public AccountNumber destination() {
        return destination;
    }

    public Money amount() {
        return amount;
    }
}
