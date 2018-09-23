package pl.slawek.restbank.infrastructure.db;

import pl.slawek.restbank.domain.*;
import pl.slawek.restbank.domain.events.AccountEvent;
import pl.slawek.restbank.domain.events.TransactionalEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MapRepositoriesImplementation implements AccountRepository, TransactionRepository {

    private Map<AccountNumber, List<AccountEvent>> accountStore = new HashMap<>();
    private Map<TransactionalMapKey, List<TransactionalEvent>> transactionStore = new HashMap<>();

    private AtomicLong accountNumberGenerator = new AtomicLong(0);
    private AtomicLong transactionIdGenerator = new AtomicLong(0);

    @Override
    public AccountNumber generateAccountNumber() {
        final long nextAccountNumber = accountNumberGenerator.incrementAndGet();
        return AccountNumber.of(Long.toString(nextAccountNumber));
    }

    @Override
    public void save(Account account) {
        storeEvents(account.notSavedEvents());
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
        TransactionalMapKey key = new TransactionalMapKey(accountNumber, transactionId);
        List<TransactionalEvent> transactionalEvents = transactionStore.get(key);
        if (transactionalEvents == null) {
            return null;
        } else {
            return Transaction.of(transactionalEvents);
        }
    }

    @Override
    public void save(Transaction transaction) {
        storeEvents(transaction.notSavedEvents());
    }

    @Override
    public List<Transaction> getAll(AccountNumber accountNumber) {
        return transactionStore.keySet()
                .stream()
                .filter(key -> key.accountNumber.equals(accountNumber))
                .map(key -> transactionStore.get(key))
                .map(Transaction::of)
                .collect(Collectors.toList());
    }


    //for test purpose
    void clean() {
        accountStore.clear();
        transactionStore.clear();
    }

    //TODO test?
    private void storeEvents(List<? extends AccountEvent> events) {
        events.stream()
                .peek(this::storeAccountEvent)
                .filter(event -> event instanceof TransactionalEvent)
                .forEach(event -> storeTransactionalEvents((TransactionalEvent) event));
    }

    private void storeAccountEvent(AccountEvent event) {
        List<AccountEvent> accountEvents = getListForEvent(event.accountNumber(), accountStore);
        accountEvents.add(event);
    }

    private void storeTransactionalEvents(TransactionalEvent event) {
        TransactionalMapKey key = new TransactionalMapKey(event.accountNumber(), event.transactionId());
        List<TransactionalEvent> accountEvents = getListForEvent(key, transactionStore);
        accountEvents.add(event);
    }

    private <E, K> List<E> getListForEvent(K key, Map<K, List<E>> map) {
        List<E> events = map.computeIfAbsent(key, k -> new ArrayList<>());
        map.put(key, events);
        return events;
    }

    private final class TransactionalMapKey {
        private final AccountNumber accountNumber;
        private final TransactionId transactionId;

        TransactionalMapKey(AccountNumber accountNumber, TransactionId transactionId) {
            this.accountNumber = accountNumber;
            this.transactionId = transactionId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TransactionalMapKey that = (TransactionalMapKey) o;
            return Objects.equals(accountNumber, that.accountNumber) &&
                    Objects.equals(transactionId, that.transactionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountNumber, transactionId);
        }
    }
}
