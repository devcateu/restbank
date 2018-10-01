package pl.slawek.restbank.infrastructure.db;

import pl.slawek.restbank.common.Lists;
import pl.slawek.restbank.domain.*;
import pl.slawek.restbank.domain.events.AccountEvent;
import pl.slawek.restbank.domain.events.TransactionalEvent;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class MapRepositoriesImplementation implements AccountRepository, TransactionRepository {

    private ConcurrentHashMap<AccountNumber, List<AccountEvent>> accountStore = new ConcurrentHashMap<>();

    private AtomicLong accountNumberGenerator = new AtomicLong(0);
    private AtomicLong transactionIdGenerator = new AtomicLong(0);

    @Override
    public AccountNumber generateAccountNumber() {
        final long nextAccountNumber = accountNumberGenerator.incrementAndGet();
        return AccountNumber.of(Long.toString(nextAccountNumber));
    }

    @Override
    public void save(Account account) {
        accountStore.merge(account.accountNumber(), new ArrayList<>(account.notSavedEvents()),
                (fromMap, fromParameter) -> mergeAccountEvent(account, fromMap, fromParameter));
    }

    @Override
    public Account getBy(AccountNumber accountNumber) {
        List<AccountEvent> accountEvents = accountStore.get(accountNumber);
        if (accountEvents == null) {
            return null;
        } else {
            return Account.of(accountEvents);
        }
    }

    @Override
    public boolean exist(AccountNumber accountNumber) {
        return accountStore.containsKey(accountNumber);
    }

    @Override
    public TransactionId generateTransactionId() {
        final long nextTransactionId = transactionIdGenerator.incrementAndGet();
        return TransactionId.of(Long.toString(nextTransactionId));
    }

    @Override
    public Transaction getBy(AccountNumber accountNumber, TransactionId transactionId) {
        List<TransactionalEvent> transactionalEvents = getEventsFor(accountStore.get(accountNumber), transactionId)
                .collect(toList());
        if (transactionalEvents.isEmpty()) {
            return null;
        } else {
            return Transaction.of(transactionalEvents);
        }
    }

    @Override
    public void save(Transaction transaction) {
        if (transaction.notSavedEvents().isEmpty()) {
            return;
        }
        accountStore.compute(transaction.transactionOwner(), (accountNumber, accountEvents) -> appendTransactionalEvent(transaction, accountEvents));
    }

    @Override
    public List<Transaction> getAll(AccountNumber accountNumber) {
        return new ArrayList<>(getTransactionalEvents(accountStore.get(accountNumber))
                .collect(groupingBy(TransactionalEvent::transactionId, collectingAndThen(toList(), Transaction::of)))
                .values());
    }

    //for test purpose
    void clean() {
        accountStore.clear();
    }

    private List<AccountEvent> appendTransactionalEvent(Transaction transaction, List<AccountEvent> accountEvents) {
        if (accountEvents == null) {
            throw new ConcurrentModificationException("Account should be saved before creating Transaction!");
        }
        final Optional<ZonedDateTime> max = getEventsFor(accountEvents, transaction.transactionId())
                .max(Comparator.comparing(AccountEvent::occurred))
                .map(AccountEvent::occurred);

        if (max.isPresent() && !max.get().equals(transaction.lastSavedEventDate())) {
            throw new ConcurrentModificationException("Before saving event somebody change Transaction!");
        }
        accountEvents.addAll(transaction.notSavedEvents());
        return accountEvents;
    }

    private List<AccountEvent> mergeAccountEvent(Account account, List<AccountEvent> fromMap, List<AccountEvent> fromParameter) {
        if (Lists.getLastElement(fromMap).occurred().equals(account.lastSavedEventDate())) {
            fromMap.addAll(fromParameter);
            return fromMap;
        } else {
            throw new ConcurrentModificationException("Noo");
        }
    }

    private Stream<TransactionalEvent> getEventsFor(List<AccountEvent> accountEvents, TransactionId transactionId) {
        return getTransactionalEvents(accountEvents)
                .filter(event -> event.transactionId().equals(transactionId));
    }

    private Stream<TransactionalEvent> getTransactionalEvents(List<AccountEvent> accountEvents) {
        return accountEvents.stream()
                .filter(event -> event instanceof TransactionalEvent)
                .map(event -> (TransactionalEvent) event);
    }
}
