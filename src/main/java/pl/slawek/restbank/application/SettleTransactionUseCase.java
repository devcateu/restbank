package pl.slawek.restbank.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.slawek.restbank.domain.*;

import java.lang.invoke.MethodHandles;

public class SettleTransactionUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TransactionRepository transactionRepository;

    public SettleTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction settle(AccountNumber accountNumber, TransactionId transactionId) throws TransactionDoesNotExist, CannotChangeStatusOfTransactionException {
        LOGGER.info("Transaction with id {} for account {} will be settled", transactionId, accountNumber);
        final Transaction transaction = transactionRepository.getBy(accountNumber, transactionId);
        validateThatTransactionExist(transaction);
        settleTransaction(transaction);
        LOGGER.info("Transaction with id {} for account {} was settled", transactionId, accountNumber);
        return transaction;
    }

    private void settleTransaction(Transaction transaction) throws CannotChangeStatusOfTransactionException {
        transaction.settle();
        transactionRepository.save(transaction);
    }

    private void validateThatTransactionExist(Transaction transaction) throws TransactionDoesNotExist {
        if(transaction == null) {
            throw new TransactionDoesNotExist();
        }
    }
}
