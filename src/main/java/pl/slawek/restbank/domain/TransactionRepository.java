package pl.slawek.restbank.domain;

import java.util.List;

public interface TransactionRepository {
    TransactionId generateTransactionId();

    Transaction getBy(AccountNumber accountNumber, TransactionId transactionId);

    void save(Transaction transaction);

    List<Transaction> getAll(AccountNumber accountNumber);
}
