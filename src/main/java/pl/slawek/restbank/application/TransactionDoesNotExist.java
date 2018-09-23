package pl.slawek.restbank.application;

import pl.slawek.restbank.common.BusinessException;

public class TransactionDoesNotExist extends BusinessException {
    public TransactionDoesNotExist() {
        super("Transaction does not exist");
    }
}
