package pl.slawek.restbank.application

import pl.slawek.revolbank.common.Money
import pl.slawek.revolbank.domain.AccountNumber
import pl.slawek.revolbank.domain.TransactionId
import pl.slawek.revolbank.domain.TransactionStatus

class RejectTransactionUseCaseTest extends BaseAccountSpecification {

    def useCase = new RejectTransactionUseCase(transactionRepository)

    def "when reject existing transaction their status should be changed to REJECTED"() {
        given:
        def accountNumber = AccountNumber.of("xzx")
        createAccountWithBalance500For(accountNumber)
        def account = accountRepository.getBy(accountNumber)
        TransactionId transactionId = makeOutgoingTransaction(account)

        when:
        useCase.reject(accountNumber, transactionId)

        then:
        def transaction = transactionRepository.getBy(accountNumber, transactionId)
        transaction.transactionStatus == TransactionStatus.REJECTED
    }

    def "when reject existing transaction money should be added into balance"() {
        given:
        def accountNumber = AccountNumber.of("xzx")
        createAccountWithBalance500For(accountNumber)
        def account = accountRepository.getBy(accountNumber)
        TransactionId transactionId = makeOutgoingTransaction(account)

        when:
        useCase.reject(accountNumber, transactionId)

        then:
        def accountWithRejectedTransaction = accountRepository.getBy(accountNumber)
        accountWithRejectedTransaction.balance() == Money.of(500)
    }

    def "when transaction does not exist should be thrown exception"() {
        given:
        def accountNumber = AccountNumber.of("xzx")
        createAccountWithBalance500For(accountNumber)

        when:
        useCase.reject(accountNumber, TransactionId.of("not existing"))

        then:
        thrown(TransactionDoesNotExist)
    }
}
