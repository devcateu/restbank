package pl.slawek.restbank.infrastructure.db

import pl.slawek.restbank.common.Money
import pl.slawek.restbank.common.OwnerId
import pl.slawek.restbank.domain.Account
import pl.slawek.restbank.domain.AccountNumber
import pl.slawek.restbank.domain.TransactionId
import spock.lang.Specification

class MapRepositoriesImplementationTest extends Specification {
    MapRepositoriesImplementation mapRepositoriesImplementation = new MapRepositoriesImplementation()

    def "should fail when transaction is saved before account "() {
        given:
        def account = new Account(OwnerId.of("owner"), AccountNumber.of("1123"))
        account.receiveIncomingTransaction(TransactionId.of("142"), Money.of(3), AccountNumber.of("53"))
        def transaction = account.makeOutgoingTransaction(TransactionId.of("c"), Money.of(1), AccountNumber.of("33"))

        when:
        transaction.settle()
        mapRepositoriesImplementation.save(transaction)

        then:
        thrown(ConcurrentModificationException)
    }

    def "should success when transaction is saved after account "() {
        given:
        def account = new Account(OwnerId.of("owner"), AccountNumber.of("1123"))
        account.receiveIncomingTransaction(TransactionId.of("142"), Money.of(3), AccountNumber.of("53"))
        def transaction = account.makeOutgoingTransaction(TransactionId.of("c"), Money.of(1), AccountNumber.of("33"))

        when:
        transaction.settle()
        mapRepositoriesImplementation.save(account)
        mapRepositoriesImplementation.save(transaction)

        then:
        noExceptionThrown()
    }

    def "should fail when transaction is saved after account "() {
        given:
        def account = new Account(OwnerId.of("owner"), AccountNumber.of("1123"))
        account.receiveIncomingTransaction(TransactionId.of("142"), Money.of(3), AccountNumber.of("53"))
        def transaction = account.makeOutgoingTransaction(TransactionId.of("c"), Money.of(1), AccountNumber.of("33"))

        when:
        mapRepositoriesImplementation.save(account)
        def accountAfterSave = mapRepositoriesImplementation.getBy(AccountNumber.of("1123"))
        transaction.settle()
        mapRepositoriesImplementation.save(transaction)
        accountAfterSave.receiveIncomingTransaction(TransactionId.of("xxx"), Money.of(3), AccountNumber.of("53"))
        mapRepositoriesImplementation.save(accountAfterSave)

        then:
        thrown(ConcurrentModificationException)
    }

    def "should allow to save two transactions"() {
        given:
        def account = new Account(OwnerId.of("owner"), AccountNumber.of("1123"))
        account.receiveIncomingTransaction(TransactionId.of("142"), Money.of(3), AccountNumber.of("53"))
        def transaction1 = account.makeOutgoingTransaction(TransactionId.of("c"), Money.of(1), AccountNumber.of("33"))
        def transaction2 = account.makeOutgoingTransaction(TransactionId.of("d"), Money.of(1), AccountNumber.of("33"))
        mapRepositoriesImplementation.save(account)

        when:
        transaction1.settle()
        transaction2.settle()
        mapRepositoriesImplementation.save(transaction1)
        mapRepositoriesImplementation.save(transaction2)

        then:
        noExceptionThrown()
    }
}
