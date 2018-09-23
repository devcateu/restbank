package pl.slawek.restbank.application


import pl.slawek.restbank.common.Money
import pl.slawek.restbank.domain.AccountNumber

import static pl.slawek.restbank.assertobject.AccountAssertObject.assertThat

class ReceiveIncomingTransactionUseCaseTest extends BaseAccountSpecification {
    private ReceiveIncomingTransactionUseCase useCase = new ReceiveIncomingTransactionUseCase(accountRepository, transactionRepository)

    def "when account exist and amount is greater then 0 transaction should be saved"() {
        given:
        def accountNumber = AccountNumber.of("xzx")
        createAccountWithBalance500For(accountNumber)

        when:
        useCase.receiveTransaction(new Transfer(AccountNumber.of("543"), accountNumber, Money.of(131)))

        then:
        def account = accountRepository.getBy(accountNumber)
        assertThat(account).hasBalance(Money.of(631))
    }

    def "when account does not exist should throw exception"() {
        given:
        def accountNumber = AccountNumber.of("xzx")

        when:
        useCase.receiveTransaction(new Transfer(AccountNumber.of("543"), accountNumber, Money.of(131)))

        then:
        thrown(DestinationAccountDoesNotExist)
    }

    def "when amount of transfer is equal 0 should throw exception"() {
        given:
        def source = AccountNumber.of("xzx")
        createAccountWithBalance500For(source)

        when:
        useCase.receiveTransaction(new Transfer(source, AccountNumber.of("xzx"), Money.of(0)))

        then:
        thrown(CannotMakeTransferForZeroException)
    }
}
