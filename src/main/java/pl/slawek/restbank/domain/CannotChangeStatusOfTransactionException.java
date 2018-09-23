package pl.slawek.restbank.domain;

import pl.slawek.restbank.common.BusinessException;

public class CannotChangeStatusOfTransactionException extends BusinessException {
    public CannotChangeStatusOfTransactionException(TransactionStatus transactionStatus) {
        super("Cannot change status of transaction, current status: " + transactionStatus);
    }
}
