package pl.slawek.restbank.application

import pl.slawek.revolbank.common.Money
import pl.slawek.revolbank.common.OwnerId
import pl.slawek.revolbank.domain.*
import pl.slawek.revolbank.infrastructure.db.MapRepositoriesImplementation
import spock.lang.Specification

abstract class BaseAccountSpecification extends Specification {
    private MapRepositoriesImplementation repository = new MapRepositoriesImplementation()
    TransactionRepository transactionRepository = repository
    AccountRepository accountRepository = repository

    def setup() {
        repository.clean()
    }

    void createAccountWithBalance500For(AccountNumber accountNumber) {
        def account = new Account(OwnerId.of("xx"), accountNumber)
        account.receiveIncomingTransaction(TransactionId.of("sample"), Money.of(500), AccountNumber.of("sample"))
        accountRepository.save(account)
    }

    TransactionId makeOutgoingTransaction(Account account) {
        def transactionId = TransactionId.of("423342")
        account.makeOutgoingTransaction(transactionId, Money.of(53), AccountNumber.of("31"))
        accountRepository.save(account)
        transactionId
    }
}
