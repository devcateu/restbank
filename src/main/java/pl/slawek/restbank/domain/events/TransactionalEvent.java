package pl.slawek.restbank.domain.events;


import pl.slawek.restbank.domain.AccountNumber;
import pl.slawek.restbank.domain.TransactionId;

public abstract class TransactionalEvent extends AccountEvent {

    private final TransactionId transactionId;

    public TransactionalEvent(AccountNumber accountNumber, TransactionId transactionId) {
        super(accountNumber);
        this.transactionId = transactionId;
    }

    public TransactionId transactionId() {
        return transactionId;
    }
}
