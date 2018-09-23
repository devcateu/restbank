package pl.slawek.restbank.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.slawek.restbank.domain.Account;
import pl.slawek.restbank.domain.AccountNumber;
import pl.slawek.restbank.domain.AccountRepository;
import pl.slawek.restbank.common.OwnerId;

import java.lang.invoke.MethodHandles;

public class CreateAccountUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AccountRepository accountRepository;

    public CreateAccountUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account create(OwnerId ownerId) {
        LOGGER.info("Adding account for {}", ownerId);
        final AccountNumber accountNumber = accountRepository.generateAccountNumber();
        final Account account = new Account(ownerId, accountNumber);
        accountRepository.save(account);
        LOGGER.info("Added account for {} with number {}", ownerId, accountNumber);
        return account;
    }
}
