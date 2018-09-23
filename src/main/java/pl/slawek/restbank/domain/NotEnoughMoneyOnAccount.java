package pl.slawek.restbank.domain;

import pl.slawek.restbank.common.BusinessException;
import pl.slawek.restbank.common.Money;

public class NotEnoughMoneyOnAccount extends BusinessException {
    public NotEnoughMoneyOnAccount(Money money) {
        super("Not enough money on account to perform transaction on " + money);
    }
}
