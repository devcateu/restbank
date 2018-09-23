package pl.slawek.restbank.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.slawek.restbank.common.Money;
import pl.slawek.restbank.domain.*;
import pl.slawek.restbank.common.BusinessException;

import java.lang.invoke.MethodHandles;

public class ReceiveIncomingTransactionUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public ReceiveIncomingTransactionUseCase(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Transaction receiveTransaction(Transfer transfer) throws BusinessException {
        LOGGER.info("Received incoming transaction from {} to {} for {}", transfer.source(), transfer.destination(), transfer.money());
        validateTransferAmountAboveZero(transfer);
        Account account = accountRepository.getBy(transfer.destination());
        validateAccountExist(account);
        final Transaction transaction = storeTransaction(transfer, account);
        LOGGER.info("Received incoming transaction from {} to {} for {} is settled", transfer.source(), transfer.destination(), transfer.money());
        return transaction;
    }

    private Transaction storeTransaction(Transfer transfer, Account account) {
        TransactionId transactionId = transactionRepository.generateTransactionId();
        final Transaction transaction = account.receiveIncomingTransaction(transactionId, transfer.money(), transfer.source());
        accountRepository.save(account);
        return transaction;
    }

    private void validateAccountExist(Account account) throws DestinationAccountDoesNotExist {
        if(account == null) {
            throw new DestinationAccountDoesNotExist();
        }
    }

    private void validateTransferAmountAboveZero(Transfer transfer) {
        if(transfer.money().equals(Money.of(0))) {
            throw new CannotMakeTransferForZeroException();
        }
    }
}
