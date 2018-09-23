package pl.slawek.restbank.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.slawek.restbank.domain.*;

import java.lang.invoke.MethodHandles;

public class RejectTransactionUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TransactionRepository transactionRepository;

    public RejectTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction reject(AccountNumber accountNumber, TransactionId transactionId) throws TransactionDoesNotExist, CannotChangeStatusOfTransactionException {
        LOGGER.info("Rejecting transaction with id {} for account {}", transactionId, accountNumber);
        final Transaction transaction = transactionRepository.getBy(accountNumber, transactionId);
        validateThatTransactionExist(transaction);
        rejectTransaction(transaction);
        LOGGER.info("Rejected transaction with id {} for account {}", transactionId, accountNumber);
        return transaction;
    }

    private void rejectTransaction(Transaction transaction) throws CannotChangeStatusOfTransactionException {
        transaction.reject();
        transactionRepository.save(transaction);
    }

    private void validateThatTransactionExist(Transaction transaction) throws TransactionDoesNotExist {
        if(transaction == null) {
            throw new TransactionDoesNotExist();
        }
    }
}
