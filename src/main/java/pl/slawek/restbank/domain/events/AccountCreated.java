package pl.slawek.restbank.domain.events;


import pl.slawek.restbank.domain.AccountNumber;
import pl.slawek.restbank.common.OwnerId;

public class AccountCreated extends AccountEvent {

    private final OwnerId ownerId;

    public AccountCreated(AccountNumber accountNumber, OwnerId ownerId) {
        super(accountNumber);
        this.ownerId = ownerId;
    }

    public OwnerId ownerId() {
        return ownerId;
    }
}
