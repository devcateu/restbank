package pl.slawek.restbank.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.slawek.restbank.common.BusinessException;
import pl.slawek.restbank.common.Money;
import pl.slawek.restbank.domain.*;

import java.lang.invoke.MethodHandles;

public class MakeOutgoingTransactionUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public MakeOutgoingTransactionUseCase(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Transaction makeTransaction(Transfer transfer) throws BusinessException {
        LOGGER.info("Making outgoing transaction from {} to {} for {}", transfer.source(), transfer.destination(), transfer.money());
        validateTransferAmountAboveZero(transfer);
        final Transaction transaction = makeOutgoingTransaction(transfer);
        LOGGER.info("Outgoing transaction was made from {} to {} for {}", transfer.source(), transfer.destination(), transfer.money());
        return transaction;
    }

    private void validateTransferAmountAboveZero(Transfer transfer) {
        if(transfer.money().equals(Money.of(0))) {
            throw new CannotMakeTransferForZeroException();
        }
    }

    private Transaction makeOutgoingTransaction(Transfer transfer) throws SourceAccountDoesNotExist, NotEnoughMoneyOnAccount {
        final Account account = accountRepository.getBy(transfer.source());
        validateAccountExist(account);
        final TransactionId transactionId = transactionRepository.generateTransactionId();
        final Transaction transaction = account.makeOutgoingTransaction(transactionId, transfer.money(), transfer.destination());
        accountRepository.save(account);
        return transaction;
    }

    private void validateAccountExist(Account account) throws SourceAccountDoesNotExist {
        if (account == null) {
            throw new SourceAccountDoesNotExist();
        }
    }
}
