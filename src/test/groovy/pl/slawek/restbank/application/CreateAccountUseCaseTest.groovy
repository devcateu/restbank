package pl.slawek.restbank.application


import pl.slawek.restbank.common.OwnerId
import pl.slawek.restbank.domain.Account

import static pl.slawek.restbank.assertobject.AccountAssertObject.assertThat

class CreateAccountUseCaseTest extends BaseAccountSpecification {
    OwnerId ownerId = OwnerId.of("42")

    def "when we create account there should the same account which we created"() {
        given:
        def useCase = new CreateAccountUseCase(accountRepository)

        when:
        def createdAccount = useCase.create(ownerId)

        then:
        Account savedAccount = accountRepository.getBy(createdAccount.accountNumber())
        assertThat(savedAccount)
                .sameAccountAs(createdAccount)
                .hasOwner(ownerId)
                .sameBalance(createdAccount)
    }

    void sameAccounts(Account createdAccount, Account savedAccount) {
        assert savedAccount.accountNumber() == createdAccount.accountNumber()
        assert savedAccount.ownerId() == createdAccount.ownerId()
    }

    void accountSameBalance(Account createdAccount, Account savedAccount) {
        assert savedAccount.balance() == createdAccount.balance()
    }

    void accountHaveOwner(Account savedAccount, OwnerId ownerId) {
        assert savedAccount.ownerId() == ownerId
    }
}
