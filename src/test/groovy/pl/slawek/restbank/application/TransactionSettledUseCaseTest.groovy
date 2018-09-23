package pl.slawek.restbank.application


import pl.slawek.restbank.domain.AccountNumber
import pl.slawek.restbank.domain.TransactionId
import pl.slawek.restbank.domain.TransactionStatus

class TransactionSettledUseCaseTest extends BaseAccountSpecification {
    private SettleTransactionUseCase useCase = new SettleTransactionUseCase(transactionRepository)

    def "when settle existing transaction their status should be changed to SETTLE"() {
        given:
        def accountNumber = AccountNumber.of("xzx")
        createAccountWithBalance500For(accountNumber)
        def account = accountRepository.getBy(accountNumber)
        TransactionId transactionId = makeOutgoingTransaction(account)

        when:
        useCase.settle(accountNumber, transactionId)

        then:
        def transaction = transactionRepository.getBy(accountNumber, transactionId)
        transaction.transactionStatus == TransactionStatus.SETTLED
    }

    def "when transaction does not exist should be thrown exception"() {
        given:
        def accountNumber = AccountNumber.of("xzx")
        createAccountWithBalance500For(accountNumber)

        when:
        useCase.settle(accountNumber, TransactionId.of("not existing"))

        then:
        thrown(TransactionDoesNotExist)
    }
}
