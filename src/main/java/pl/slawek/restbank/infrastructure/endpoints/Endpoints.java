package pl.slawek.restbank.infrastructure.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.slawek.restbank.application.*;
import pl.slawek.restbank.common.*;
import pl.slawek.restbank.domain.*;
import pl.slawek.restbank.infrastructure.endpoints.data.*;
import pl.slawek.restbank.infrastructure.endpoints.rest.EndpointDefinition;
import pl.slawek.restbank.infrastructure.endpoints.rest.HttpMethod;
import pl.slawek.restbank.infrastructure.endpoints.rest.Link;
import pl.slawek.restbank.infrastructure.endpoints.rest.RestfullResponse;
import spark.*;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.function.Function;

import static java.net.HttpURLConnection.*;
import static java.util.stream.Collectors.toList;
import static spark.Spark.*;

public class Endpoints {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String CONTENT_TYPE = "application/json";
    private static final EndpointDefinition CREATE_ACCOUNT = new EndpointDefinition("/account/", HttpMethod.POST);
    private static final EndpointDefinition GET_ACCOUNT = new EndpointDefinition("/account/:accountNumber", HttpMethod.GET);
    private static final EndpointDefinition CREATE_OUTGOING_TRANSACTION = new EndpointDefinition("/account/:accountNumber/transaction/", HttpMethod.POST);
    private static final EndpointDefinition CREATE_INCOMING_TRANSACTION = new EndpointDefinition("/account/:accountNumber/income/", HttpMethod.POST);
    private static final EndpointDefinition GET_ALL_TRANSACTIONS = new EndpointDefinition("/account/:accountNumber/transaction/", HttpMethod.GET);
    private static final EndpointDefinition GET_TRANSACTION = new EndpointDefinition("/account/:accountNumber/transaction/:transactionId", HttpMethod.GET);
    private static final EndpointDefinition SETTLE_TRANSACTION = new EndpointDefinition("/account/:accountNumber/transaction/:transactionId/settle", HttpMethod.POST);
    private static final EndpointDefinition REJECT_TRANSACTION = new EndpointDefinition("/account/:accountNumber/transaction/:transactionId/settle", HttpMethod.DELETE);
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CreateAccountUseCase createAccountUseCase;
    private final MakeOutgoingTransactionUseCase makeOutgoingTransactionUseCase;
    private final ReceiveIncomingTransactionUseCase receiveIncomingTransactionUseCase;
    private final RejectTransactionUseCase rejectTransactionUseCase;
    private final SettleTransactionUseCase settleTransactionUseCase;
    private ObjectMapper objectMapper = new ObjectMapper();

    public Endpoints(TransactionRepository transactionRepository,
                     AccountRepository accountRepository,
                     CreateAccountUseCase createAccountUseCase,
                     MakeOutgoingTransactionUseCase makeOutgoingTransactionUseCase,
                     ReceiveIncomingTransactionUseCase receiveIncomingTransactionUseCase,
                     RejectTransactionUseCase rejectTransactionUseCase,
                     SettleTransactionUseCase settleTransactionUseCase) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.createAccountUseCase = createAccountUseCase;
        this.makeOutgoingTransactionUseCase = makeOutgoingTransactionUseCase;
        this.receiveIncomingTransactionUseCase = receiveIncomingTransactionUseCase;
        this.rejectTransactionUseCase = rejectTransactionUseCase;
        this.settleTransactionUseCase = settleTransactionUseCase;
    }

    public void registerAllEndpoints() {
        after((Filter) (request, response) -> response.type(CONTENT_TYPE));
        registerEndpoint(CREATE_ACCOUNT, this::createAccount);
        registerEndpoint(GET_ACCOUNT, this::getAccount);
        registerEndpoint(CREATE_OUTGOING_TRANSACTION, this::createOutgoingTransaction);
        registerEndpoint(CREATE_INCOMING_TRANSACTION, this::createIncomingTransaction);
        registerEndpoint(GET_ALL_TRANSACTIONS, this::getAllTransactions);
        registerEndpoint(GET_TRANSACTION, this::getTransaction);
        registerEndpoint(SETTLE_TRANSACTION, (req, res) -> settleTransaction(req));
        registerEndpoint(REJECT_TRANSACTION, (req, res) -> rejectTransaction(req));

        exception(MismatchedInputException.class, (e, request, response) -> bedRequest(e, response, ex -> "Wrong input format"));
        exception(BusinessException.class, (e, request, response) -> bedRequest(e, response, Throwable::getMessage));
        exception(ValidationException.class, (e, request, response) -> bedRequest(e, response, Throwable::getMessage));
        exception(ConcurrentModificationException.class, (e, request, response) -> {
            LOGGER.error("Concurrency Modification", e);
            exceptionBodyWriter(response, e.getMessage());
            response.status(HTTP_CONFLICT);
        });
        exception(Exception.class, (e, request, response) -> {
            LOGGER.error("Internal server error", e);
            exceptionBodyWriter(response, e.getMessage());
            response.status(HTTP_INTERNAL_ERROR);
        });
    }

    private Object rejectTransaction(Request req) throws TransactionDoesNotExist, CannotChangeStatusOfTransactionException {
        final AccountNumber accountNumber = getAccountNumber(req);
        final TransactionId transactionId = getTransactionId(req);
        final Transaction transaction = rejectTransactionUseCase.reject(accountNumber, transactionId);
        return mapToResponse(transaction);
    }

    private Object settleTransaction(Request req) throws TransactionDoesNotExist, CannotChangeStatusOfTransactionException {
        final AccountNumber accountNumber = getAccountNumber(req);
        final TransactionId transactionId = getTransactionId(req);
        final Transaction transaction = settleTransactionUseCase.settle(accountNumber, transactionId);
        return mapToResponse(transaction);
    }

    private Object getTransaction(Request req, Response res) {
        final AccountNumber accountNumber = getAccountNumber(req);
        final TransactionId transactionId = getTransactionId(req);
        final Transaction transaction = transactionRepository.getBy(accountNumber, transactionId);

        if (transaction == null) {
            res.status(HTTP_NOT_FOUND);
            return getMessage("Transaction Not Found");
        }
        return mapToResponse(transaction);
    }

    private Object getAllTransactions(Request req, Response res) {
        final AccountNumber accountNumber = getAccountNumber(req);
        if (!accountRepository.exist(accountNumber)) {
            res.status(HTTP_NOT_FOUND);
            return getMessage("Account Not Found");
        }
        final List<Transaction> transaction = transactionRepository.getAll(accountNumber);
        return getResponseForListOfTransactions(transaction.stream()
                .map(this::mapToResponse)
                .collect(toList()), accountNumber);
    }

    private Object createIncomingTransaction(Request req, Response res) throws java.io.IOException, BusinessException {
        final AccountNumber accountNumber = getAccountNumber(req);
        final MakeIncomingTransactionData makeIncomingTransactionData = mapBody(req, MakeIncomingTransactionData.class);
        final Transaction transaction = makeIncomingTransaction(accountNumber, makeIncomingTransactionData);
        res.status(HTTP_CREATED);
        return mapToResponse(transaction);
    }

    private Object createOutgoingTransaction(Request req, Response res) throws java.io.IOException, BusinessException {
        final AccountNumber accountNumber = getAccountNumber(req);
        final MakeOutgoingTransactionData makeOutgoingTransactionData = mapBody(req, MakeOutgoingTransactionData.class);
        final Transaction transaction = makeOutgoingTransaction(accountNumber, makeOutgoingTransactionData);
        res.status(HTTP_CREATED);
        return mapToResponse(transaction);
    }

    private Object getAccount(Request req, Response res) {
        final AccountNumber accountNumber = getAccountNumber(req);
        final Account account = accountRepository.getBy(accountNumber);
        if (account == null) {
            res.status(HTTP_NOT_FOUND);
            return getMessage("Account Not Found");
        }
        return mapToResponse(account);
    }

    private Object createAccount(Request req, Response res) throws java.io.IOException {
        final CreateAccount createAccount = mapBody(req, CreateAccount.class);
        final Account account = createAccountUseCase.create(OwnerId.of(createAccount.getOwner()));
        res.status(HTTP_CREATED);
        return mapToResponse(account);
    }

    private void bedRequest(Exception e, Response response, Function<Exception, String> messageExtractor) {
        LOGGER.warn("Wrong user data", e);
        exceptionBodyWriter(response, messageExtractor.apply(e));
        response.status(HTTP_BAD_REQUEST);
    }

    private void exceptionBodyWriter(spark.Response response, String message) {
        response.type(CONTENT_TYPE);
        response.body(getMessage(message));
    }

    private String getMessage(String message) {
        return "{ \"message\" : \"" + message + "\" }";
    }

    private Transaction makeIncomingTransaction(AccountNumber accountNumber, MakeIncomingTransactionData makeIncomingTransactionData) throws BusinessException {
        final AccountNumber source = AccountNumber.of(makeIncomingTransactionData.getSource());
        final Money amount = Money.of(makeIncomingTransactionData.getAmount());
        final Transfer transfer = new Transfer(source, accountNumber, amount);
        return receiveIncomingTransactionUseCase.receiveTransaction(transfer);
    }

    private Transaction makeOutgoingTransaction(AccountNumber accountNumber, MakeOutgoingTransactionData makeOutgoingTransactionData) throws BusinessException {
        final Money amount = Money.of(makeOutgoingTransactionData.getAmount());
        final AccountNumber destination = AccountNumber.of(makeOutgoingTransactionData.getDestination());
        final Transfer transfer = new Transfer(accountNumber, destination, amount);
        return makeOutgoingTransactionUseCase.makeTransaction(transfer);
    }

    private <T> T mapBody(Request req, Class<T> valueType) throws java.io.IOException {
        final String body = req.body();
        return objectMapper.readValue(body, valueType);
    }

    private TransactionId getTransactionId(Request req) {
        final String transactionId = req.params(":transactionId");
        return TransactionId.of(transactionId);
    }

    private AccountNumber getAccountNumber(Request req) {
        final String accountNumber = req.params(":accountNumber");
        return AccountNumber.of(accountNumber);
    }

    private RestfullResponse getResponseForListOfTransactions(List<RestfullResponse> transactions, AccountNumber accountNumber) {
        final ArrayList<Link> links = new ArrayList<>();
        links.add(createLink(GET_ACCOUNT, "account", accountNumber));
        return new RestfullResponse(transactions, links);
    }

    private RestfullResponse mapToResponse(Transaction transaction) {
        final TransactionData transactionData = new TransactionData(transaction);
        final ArrayList<Link> links = new ArrayList<>();
        links.add(createLink(GET_TRANSACTION, "self", transaction));
        links.add(createLink(GET_ACCOUNT, "account", transaction));
        if (transaction.transactionStatus() == TransactionStatus.CREATED) {
            links.add(createLink(SETTLE_TRANSACTION, "settle", transaction));
            links.add(createLink(REJECT_TRANSACTION, "reject", transaction));
        }
        return new RestfullResponse(transactionData, links);
    }

    private RestfullResponse mapToResponse(Account account) {
        final AccountData accountData = new AccountData(account);

        final ArrayList<Link> links = new ArrayList<>();
        links.add(createLink(GET_ACCOUNT, "self", account));
        links.add(createLink(GET_ALL_TRANSACTIONS, "transactions", account));
        links.add(createLink(CREATE_OUTGOING_TRANSACTION, "makeOutgoingTransaction", account));
        links.add(createLink(CREATE_INCOMING_TRANSACTION, "receiveIncomingTransaction", account));
        return new RestfullResponse(accountData, links);
    }

    private Link createLink(EndpointDefinition endpointDefinition, String relation, Object object) {
        final String type = endpointDefinition.httpMethod().name();
        final String href = resolveHref(endpointDefinition.path(), object);
        return new Link(type, href, relation);
    }

    private String resolveHref(String href, Object object) {
        if (object instanceof Account) {
            return prepareHref(href, (Account) object);
        } else if (object instanceof AccountNumber) {
            return prepareHref(href, (AccountNumber) object);
        } else if (object instanceof Transaction) {
            return prepareHref(href, (Transaction) object);
        } else {
            throw new UnsupportedObjectType(object);
        }
    }

    private String prepareHref(String link, Transaction transaction) {
        return prepareHref(link, transaction.transactionOwner())
                .replace(":transactionId", transaction.transactionId().toString());
    }

    private String prepareHref(String link, Account account) {
        final AccountNumber accountNumber = account.accountNumber();
        return prepareHref(link, accountNumber);
    }

    private String prepareHref(String link, AccountNumber accountNumber) {
        return "http://localhost:" + Spark.port() +
                link.replace(":accountNumber", accountNumber.toString());
    }

    private void registerEndpoint(EndpointDefinition endpointDefinition, Route route) {
        switch (endpointDefinition.httpMethod()) {
            case GET:
                get(endpointDefinition.path(), CONTENT_TYPE, route, this::writeAsJson);
                break;
            case POST:
                post(endpointDefinition.path(), CONTENT_TYPE, route, this::writeAsJson);
                break;
            case DELETE:
                delete(endpointDefinition.path(), CONTENT_TYPE, route, this::writeAsJson);
                break;
            default:
                throw new NotSupportedHttpMethod(endpointDefinition.httpMethod());
        }
    }

    private String writeAsJson(Object model) throws JsonProcessingException {
        return objectMapper.writeValueAsString(model);
    }

    private class NotSupportedHttpMethod extends ProgrammaticException {
        public NotSupportedHttpMethod(HttpMethod httpMethod) {
            super("You are trying register not supported HttpMethod: " + httpMethod);
        }
    }

    private class UnsupportedObjectType extends ProgrammaticException {
        public UnsupportedObjectType(Object object) {
            super("You are trying link unsupported object type: " + object.getClass());
        }
    }
}
