package pl.slawek.restbank.domain;

import pl.slawek.restbank.common.Money;
import pl.slawek.restbank.common.OwnerId;
import pl.slawek.restbank.common.Validations;
import pl.slawek.restbank.domain.events.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Account {
    private OwnerId ownerId;
    private AccountNumber accountNumber;
    private List<AccountEvent> accountEvents = new ArrayList<>();
    private Money balance = Money.of(0);
    private ZonedDateTime lastUpdate;

    private Account() {

    }

    public Account(OwnerId ownerId, AccountNumber accountNumber) {
        applyEvent(new AccountCreated(accountNumber, ownerId));
        validate();
    }

    public static Account of(List<AccountEvent> events) {
        final Account account = new Account();
        for (AccountEvent event : events) {
            account.applyEvent(event);
        }
        account.validate();
        account.accountEvents.clear();
        return account;
    }

    public Transaction makeOutgoingTransaction(TransactionId transactionId, Money amount, AccountNumber destination) throws NotEnoughMoneyOnAccount {
        validateIfThereIsEnoughMoney(amount);
        final OutgoingTransaction transaction = new OutgoingTransaction(accountNumber, transactionId, destination, amount);
        applyEvent(transaction);
        return Transaction.of(Collections.singletonList(transaction));
    }

    public Transaction receiveIncomingTransaction(TransactionId transactionId, Money amount, AccountNumber source) {
        final IncomingTransaction transaction = new IncomingTransaction(accountNumber, transactionId, source, amount);
        applyEvent(transaction);
        return Transaction.of(Collections.singletonList(transaction));
    }

    public OwnerId ownerId() {
        return ownerId;
    }

    public AccountNumber accountNumber() {
        return accountNumber;
    }

    public Money balance() {
        return balance;
    }

    public ZonedDateTime lastUpdate() {
        return lastUpdate;
    }

    public List<AccountEvent> notSavedEvents() {
        return Collections.unmodifiableList(accountEvents);
    }

    private void applyEvent(AccountEvent accountEvent) {
        if (accountEvent instanceof AccountCreated) {
            accountCreating((AccountCreated) accountEvent);
        } else if (accountEvent instanceof TransactionalEvent) {
            addTransaction((TransactionalEvent) accountEvent);
        } else {
            throw new UnsupportedAccountEventException();
        }
        this.lastUpdate = accountEvent.occurred();
        accountEvents.add(accountEvent);
    }

    private void validate() {
        Validations.requireNotNull(ownerId, "owner");
        Validations.requireNotNull(accountNumber, "accountNumber");
        Validations.requireNotNull(lastUpdate, "lastUpdate");
    }

    private void accountCreating(AccountCreated accountEvent) {
        this.ownerId = Validations.requireNotNull(accountEvent.ownerId(), "owner");
        this.accountNumber = Validations.requireNotNull(accountEvent.accountNumber(), "accountNumber");
        this.balance = Money.of(0);
    }

    private void addTransaction(TransactionalEvent transaction) {
        if (transaction instanceof IncomingTransaction) {
            balance = balance.add(((IncomingTransaction) transaction).amount());
        } else if (transaction instanceof OutgoingTransaction) {
            balance = balance.minus(((OutgoingTransaction) transaction).amount());
        } else if (transaction instanceof TransactionSettled) {
            //no action
        } else if (transaction instanceof TransactionRejected) {
            balance = balance.add(((TransactionRejected) transaction).amount());
        } else {
            throw new UnsupportedTransactionEventException();
        }
    }

    private void validateIfThereIsEnoughMoney(Money amount) throws NotEnoughMoneyOnAccount {
        if (amount.isGreaterThan(balance)) {
            throw new NotEnoughMoneyOnAccount(amount);
        }
    }
}
