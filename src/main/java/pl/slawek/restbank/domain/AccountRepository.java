package pl.slawek.restbank.domain;

public interface AccountRepository {
    AccountNumber generateAccountNumber();

    void save(Account account);

    Account getBy(AccountNumber accountNumber);

    boolean exist(AccountNumber accountNumber);

}
