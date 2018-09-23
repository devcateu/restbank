package pl.slawek.restbank.domain


import pl.slawek.restbank.common.Money
import pl.slawek.restbank.common.OwnerId
import spock.lang.Specification

import static pl.slawek.restbank.domain.TransactionStatus.CREATED

class AccountTest extends Specification {

    def SOME_ACCOUNT_NUMBER = AccountNumber.of("342")
    def OWNER_ACCOUNT_NUMBER = AccountNumber.of("123")
    def SOME_TRANSACTION_ID = TransactionId.of("54")

    def "outgoing transaction should take TransactionId from parameter"() {
        given:
        def account = createAccountWith500Credit()

        when:
        def transaction = account.makeOutgoingTransaction(TransactionId.of("4623"), Money.of(123), SOME_ACCOUNT_NUMBER)

        then:
        transaction.transactionId() == TransactionId.of("4623")
    }

    def "source of transaction should be taken from account for outgoing transaction"() {
        given:
        def account = createAccountWith500Credit()

        when:
        def transaction = account.makeOutgoingTransaction(SOME_TRANSACTION_ID, Money.of(123), SOME_ACCOUNT_NUMBER)

        then:
        transaction.source() == OWNER_ACCOUNT_NUMBER
    }

    def "money of transaction will be taken from parameter of outgoing transaction"() {
        given:
        def account = createAccountWith500Credit()

        when:
        def transaction = account.makeOutgoingTransaction(SOME_TRANSACTION_ID, Money.of(123), SOME_ACCOUNT_NUMBER)

        then:
        transaction.amount() == Money.of(123)
    }

    def "destination of transaction will be taken from parameter of outgoing transaction"() {
        given:
        def account = createAccountWith500Credit()

        when:
        def transaction = account.makeOutgoingTransaction(SOME_TRANSACTION_ID, Money.of(123), SOME_ACCOUNT_NUMBER)

        then:
        transaction.destination() == SOME_ACCOUNT_NUMBER
    }

    def "when we make outgoing transaction status should be CREATED"() {
        given:
        def account = createAccountWith500Credit()

        when:
        def transaction = account.makeOutgoingTransaction(SOME_TRANSACTION_ID, Money.of(11), SOME_ACCOUNT_NUMBER)

        then:
        transaction.transactionStatus() == CREATED
    }

    def "when we make outgoing transaction source should be number from account"() {
        given:
        def account = createAccountWith500Credit()

        when:
        def transaction = account.makeOutgoingTransaction(SOME_TRANSACTION_ID, Money.of(11), SOME_ACCOUNT_NUMBER)

        then:
        transaction.source() == OWNER_ACCOUNT_NUMBER
    }

    def "when we make outgoing transaction balance of account should decrease by this amount"() {
        given:
        def account = createAccountWith500Credit()

        when:
        account.makeOutgoingTransaction(SOME_TRANSACTION_ID, Money.of(11), SOME_ACCOUNT_NUMBER)

        then:
        account.balance() == Money.of(489)
    }

    def "when we make outgoing transaction the amount of that should not be bigger than our balance"() {
        given:
        def account = createAccountWith500Credit()

        when:
        account.makeOutgoingTransaction(SOME_TRANSACTION_ID, Money.of(600), SOME_ACCOUNT_NUMBER)

        then:
        thrown(NotEnoughMoneyOnAccount)
    }

    def "when we settle outgoing transaction the status should change into SETTLED"() {
        given:
        def account = createAccountWith500Credit()
        def transaction = account.makeOutgoingTransaction(SOME_TRANSACTION_ID, Money.of(23), SOME_ACCOUNT_NUMBER)

        when:
        transaction.settle()

        then:
        transaction.transactionStatus() == TransactionStatus.SETTLED
    }

    def "when we reject outgoing transaction the status should change into REJECTED"() {
        given:
        def account = createAccountWith500Credit()
        def transaction = account.makeOutgoingTransaction(SOME_TRANSACTION_ID, Money.of(23), SOME_ACCOUNT_NUMBER)

        when:
        transaction.reject()

        then:
        transaction.transactionStatus() == TransactionStatus.REJECTED
    }

    def "when we reject rejected transaction the exception should be thrown"() {
        given:
        def account = createAccountWith500Credit()
        def transaction = account.makeOutgoingTransaction(SOME_TRANSACTION_ID, Money.of(23), SOME_ACCOUNT_NUMBER)
        transaction.reject()

        when:
        transaction.reject()

        then:
        thrown(CannotChangeStatusOfTransactionException)
    }

    def "when we settle rejected transaction the exception should be thrown"() {
        given:
        def account = createAccountWith500Credit()
        def transaction = account.makeOutgoingTransaction(SOME_TRANSACTION_ID, Money.of(23), SOME_ACCOUNT_NUMBER)
        transaction.reject()

        when:
        transaction.settle()

        then:
        thrown(CannotChangeStatusOfTransactionException)
    }

    def "when incoming transaction is received their status should be SETTLED"() {
        given:
        def account = createAccountWith500Credit()

        when:
        def transaction = account.receiveIncomingTransaction(SOME_TRANSACTION_ID, Money.of(123), SOME_ACCOUNT_NUMBER)

        then:
        transaction.transactionStatus() == TransactionStatus.SETTLED
    }

    def "when incoming transaction is received balance of account should increase by this amount"() {
        given:
        def account = createAccountWith500Credit()

        when:
        account.receiveIncomingTransaction(SOME_TRANSACTION_ID, Money.of(123), SOME_ACCOUNT_NUMBER)

        then:
        account.balance() == Money.of(623)
    }

    def "when incoming transaction is received their source should be passed via parameter"() {
        given:
        def account = createAccountWith500Credit()

        when:
        def transaction = account.receiveIncomingTransaction(SOME_TRANSACTION_ID, Money.of(123), SOME_ACCOUNT_NUMBER)

        then:
        transaction.source() == SOME_ACCOUNT_NUMBER
    }

    def "when incoming transaction is received their destination should be taken from account"() {
        given:
        def account = createAccountWith500Credit()

        when:
        def transaction = account.receiveIncomingTransaction(SOME_TRANSACTION_ID, Money.of(123), SOME_ACCOUNT_NUMBER)

        then:
        transaction.destination() == account.accountNumber()
    }

    def "when incoming transaction is received their amount should be passed via parameter"() {
        given:
        def account = createAccountWith500Credit()

        when:
        def transaction = account.receiveIncomingTransaction(SOME_TRANSACTION_ID, Money.of(123), SOME_ACCOUNT_NUMBER)

        then:
        transaction.amount() == Money.of(123)
    }

    def createAccountWith500Credit() {
        def account = new Account(OwnerId.of("login"), OWNER_ACCOUNT_NUMBER)
        account.receiveIncomingTransaction(TransactionId.of("3"), Money.of(500), SOME_ACCOUNT_NUMBER)
        return account
    }
}

