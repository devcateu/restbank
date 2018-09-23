package pl.slawek.restbank.infrastructure.endpoints

import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import pl.slawek.revolbank.infrastructure.ApplicationRunner
import spark.Spark
import spock.lang.Shared
import spock.lang.Specification

import static groovyx.net.http.ContentType.JSON

class APITest extends Specification {

    @Shared
    RESTClient restClient
    def setupSpec() {
        ApplicationRunner.main()
        restClient = new RESTClient()
        restClient.handler.failure = restClient.handler.success
    }

    def cleanupSpec() {
        Spark.stop()
    }

    def "should register sensor"() {
        when:
        def response = registerAccount()

        then: "Status is 201"
        response.status == 201
    }

    def "trying registering account with wrong body should result in response with status 400 and message in body"() {
        when:
        def response = registerAccountWithBody([])

        then: "Status is 400"
        response.status == 400

        and: "message that owner is missing"
        response.data.message == "Wrong message"
    }

    def "trying registering account without owner should result in response with status 400 and information about lack owner"() {
        when:
        def response = registerAccountWithBody([owner: null])

        then: "Status is 400"
        response.status == 400

        and: "message that owner is missing"
        response.data.message.contains("owner")
        response.data.message.contains("Required parameter is empty")
    }

    def "get should return same response as registering account"() {
        given:
        def accountResponse = registerAccount()

        when:
        def responseOfGetAccount = makeRequestForRelationFromResponse(accountResponse, "self") {}

        then: "Status is 200"
        responseOfGetAccount.status == 200

        and: "body of response is the same as from account creation"
        responseOfGetAccount.data == accountResponse.data
    }

    def "should make incoming transaction"() {
        given:
        def accountResponse = registerAccount()

        when:
        def responseIncomingTransaction = makeIncomingTransaction(accountResponse)

        then: "Status is 201"
        responseIncomingTransaction.status == 201
    }

    def "should make outgoing transaction"() {
        given:
        def accountResponse = registerAccount()
        makeIncomingTransaction(accountResponse)

        when:
        def responseOutgoingTransaction = makeOutgoingTransaction(accountResponse)

        then: "Status is 201"
        responseOutgoingTransaction.status == 201
    }

    def "when not enough money for transaction should return status 400 with reason"() {
        given:
        def accountResponse = registerAccount()

        when:
        def responseOutgoingTransaction = makeOutgoingTransaction(accountResponse)

        then: "Status is 400"
        responseOutgoingTransaction.status == 400

        and: "Message that not enough money"
        responseOutgoingTransaction.data.message.contains("Not enough money on account to perform transaction")
    }

    def "should settle outgoing transaction"() {
        given:
        def accountResponse = registerAccount()
        makeIncomingTransaction(accountResponse)
        def responseOutgoingTransaction = makeOutgoingTransaction(accountResponse)

        when:
        def settledTransaction = makeRequestForRelationFromResponse(responseOutgoingTransaction, "settle") {}

        then: "Status is 200"
        settledTransaction.status == 200
    }

    def "should reject outgoing transaction"() {
        given:
        def accountResponse = registerAccount()
        makeIncomingTransaction(accountResponse)
        def responseOutgoingTransaction = makeOutgoingTransaction(accountResponse)

        when:
        def settledTransaction = makeRequestForRelationFromResponse(responseOutgoingTransaction, "reject") {}

        then: "Status is 200"
        settledTransaction.status == 200
    }

    def "when making 2 transaction both should be on the list of al transactions"() {
        given:
        def accountResponse = registerAccount()
        def incomingTransaction = makeIncomingTransaction(accountResponse)
        def outgoingTransaction = makeOutgoingTransaction(accountResponse)

        when:
        def allTransactionsResponse = makeRequestForRelationFromResponse(accountResponse, "transactions") {}

        then: "Status is 200"
        allTransactionsResponse.status == 200

        and: "Contain incoming transaction & outgoing"
        allTransactionsResponse.data.data.size == 2
        allTransactionsResponse.data.data.contains(incomingTransaction.data)
        allTransactionsResponse.data.data.contains(outgoingTransaction.data)

    }

    def "result of creating incoming transaction and get self should return same transaction"() {
        given:
        def accountResponse = registerAccount()
        def responseIncomingTransaction = makeIncomingTransaction(accountResponse)

        when:
        def selfTransaction = makeRequestForRelationFromResponse(responseIncomingTransaction, "self") {}

        then: "Status is 200"
        selfTransaction.status == 200

        and: "body of response is the same as from transaction"
        selfTransaction.data == responseIncomingTransaction.data
    }

    private def makeOutgoingTransaction(accountResponse) {
        makeRequestForRelationFromResponse(accountResponse, "makeOutgoingTransaction") {
            body = [destination: "5331", amount: 2.2]
        }
    }

    private def makeIncomingTransaction(accountResponse) {
        makeRequestForRelationFromResponse(accountResponse, "receiveIncomingTransaction") {
            body = [source: "423", amount: 532.2]
        }
    }

    private def registerAccount() {
        def createAccountBody = [owner: "Han Solo"]
        registerAccountWithBody(createAccountBody)
    }

    private def registerAccountWithBody(createAccountBody) {
        restClient.post(
                uri: "http://localhost:4567/account/",
                body: createAccountBody,
                requestContentType: JSON
        )
    }

    private def makeRequestForRelationFromResponse(response, rel, Closure configClosure) {
        def link = getLinkFor(response, rel)
        restClient.request(link.href, Method.valueOf(link.type), JSON, configClosure)
    }

    private def getLinkFor(response, rel) {
        for(def link : response.data.links) {
            if(link.rel == rel) {
                return link
            }
        }
        throw new LinkNotFoundException()
    }

    private static class LinkNotFoundException extends RuntimeException {

    }
}
