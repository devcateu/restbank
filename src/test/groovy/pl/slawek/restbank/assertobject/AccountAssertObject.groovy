package pl.slawek.restbank.assertobject

import pl.slawek.restbank.common.Money
import pl.slawek.restbank.common.OwnerId
import pl.slawek.restbank.domain.Account

class AccountAssertObject {
    private Account account;

    private AccountAssertObject(Account account) {
        this.account = account
    }

    static assertThat(Account account) {
        new AccountAssertObject(account)
    }

    AccountAssertObject sameAccountAs(Account otherAccount) {
        assert account.accountNumber() == otherAccount.accountNumber()
        assert account.ownerId() == otherAccount.ownerId()
        return this
    }

    AccountAssertObject sameBalance(Account otherAccount) {
        assert account.balance() == otherAccount.balance()
        return this
    }

    AccountAssertObject hasOwner(OwnerId ownerId) {
        assert account.ownerId() == ownerId
        return this
    }

    AccountAssertObject hasBalance(Money balance) {
        assert account.balance() == balance
        return this
    }
}
