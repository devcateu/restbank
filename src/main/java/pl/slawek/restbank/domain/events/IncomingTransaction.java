package pl.slawek.restbank.domain.events;

import pl.slawek.restbank.common.Money;
import pl.slawek.restbank.domain.*;

public final class IncomingTransaction extends TransactionalEvent   {
    private final AccountNumber source;
    private final Money amount;

    public IncomingTransaction(AccountNumber owner, TransactionId transactionId, AccountNumber source, Money amount) {
        super(owner, transactionId);
        this.source = source;
        this.amount = amount;
    }

    public AccountNumber source() {
        return source;
    }

    public Money amount() {
        return amount;
    }
}
