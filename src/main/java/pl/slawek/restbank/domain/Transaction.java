package pl.slawek.restbank.domain;

import pl.slawek.restbank.common.Money;
import pl.slawek.restbank.common.Validations;
import pl.slawek.restbank.domain.events.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Transaction {
    private AccountNumber transactionOwner;
    private TransactionId transactionId;
    private AccountNumber source;
    private AccountNumber destination;
    private Money amount;
    private TransactionStatus transactionStatus;
    private ZonedDateTime modificationDate;
    private List<TransactionalEvent> eventsToSave = new ArrayList<>();

    private Transaction() {

    }

    public static Transaction of(List<TransactionalEvent> events) {
        final Transaction transaction = new Transaction();
        events.forEach(transaction::apply);
        transaction.validate();
        transaction.eventsToSave.clear();
        return transaction;
    }

    public void reject() throws CannotChangeStatusOfTransactionException {
        validateThatCanChangeTransactionStatus();

        apply(new TransactionRejected(transactionOwner, transactionId, amount));
    }

    public void settle() throws CannotChangeStatusOfTransactionException {
        validateThatCanChangeTransactionStatus();

        apply(new TransactionSettled(transactionOwner, transactionId, amount));
    }

    public AccountNumber transactionOwner() {
        return transactionOwner;
    }

    public AccountNumber source() {
        return source;
    }

    public AccountNumber destination() {
        return destination;
    }

    public Money amount() {
        return amount;
    }

    public TransactionStatus transactionStatus() {
        return transactionStatus;
    }

    public TransactionId transactionId() {
        return transactionId;
    }

    public List<TransactionalEvent> notSavedEvents() {
        return Collections.unmodifiableList(eventsToSave);
    }

    public ZonedDateTime modificationDate() {
        return modificationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId) &&
                Objects.equals(source, that.source) &&
                Objects.equals(destination, that.destination) &&
                Objects.equals(amount, that.amount) &&
                transactionStatus == that.transactionStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, source, destination, amount, transactionStatus);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", source=" + source +
                ", destination=" + destination +
                ", amount=" + amount +
                ", transactionStatus=" + transactionStatus +
                '}';
    }

    private void validate() {
        Validations.requireNotNull(this.source, "source");
        Validations.requireNotNull(this.destination, "destination");
        Validations.requireNotNull(this.transactionStatus, "transactionStatus");
        Validations.requireNotNull(this.transactionId, "transactionId");
        Validations.requireNotNull(this.amount, "amount");
        Validations.requireNotNull(this.transactionOwner, "transactionOwner");
        Validations.requireNotNull(this.modificationDate, "transactionDate");
    }

    private void apply(TransactionalEvent event) {
        if (event instanceof IncomingTransaction) {
            applyIncomingTransaction((IncomingTransaction) event);
        } else if (event instanceof OutgoingTransaction) {
            applyOutgoingTransaction((OutgoingTransaction) event);
        } else if (event instanceof TransactionSettled) {
            settleTransaction((TransactionSettled) event);
        } else if (event instanceof TransactionRejected) {
            rejectTransaction((TransactionRejected) event);
        } else {
            throw new UnsupportedTransactionEventException();
        }
        this.modificationDate = event.occurred();
        eventsToSave.add(event);
    }

    private void rejectTransaction(TransactionRejected event) {
        transactionStatus = TransactionStatus.REJECTED;
    }

    private void settleTransaction(TransactionSettled event) {
        transactionStatus = TransactionStatus.SETTLED;
    }

    private void validateThatCanChangeTransactionStatus() throws CannotChangeStatusOfTransactionException {
        if (transactionStatus != TransactionStatus.CREATED) {
            throw new CannotChangeStatusOfTransactionException(transactionStatus);
        }
    }

    private void applyIncomingTransaction(IncomingTransaction event) {
        this.source = event.source();
        this.destination = event.accountNumber();
        this.transactionStatus = TransactionStatus.SETTLED;
        this.transactionId = event.transactionId();
        this.amount = event.amount();
        this.transactionOwner = event.accountNumber();
    }

    private void applyOutgoingTransaction(OutgoingTransaction event) {
        this.source = event.accountNumber();
        this.destination = event.destination();
        this.transactionStatus = TransactionStatus.CREATED;
        this.transactionId = event.transactionId();
        this.amount = event.amount();
        this.transactionOwner = event.accountNumber();
    }
}
