package pl.slawek.restbank.infrastructure.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.slawek.restbank.application.*;
import pl.slawek.restbank.common.BusinessException;
import pl.slawek.restbank.common.Money;
import pl.slawek.restbank.common.OwnerId;
import pl.slawek.restbank.common.ValidationException;
import pl.slawek.restbank.domain.*;
import spark.Filter;
import spark.Request;
import spark.Spark;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.util.stream.Collectors.toList;
import static spark.Spark.*;

public class Endpoints {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String CONTENT_TYPE = "application/json";
    private static final String CREATE_ACCOUNT_LINK = "/account/";
    private static final String GET_ACCOUNT_LINK = "/account/:accountNumber";
    private static final String CREATE_OUTGOING_TRANSACTION_LINK = "/account/:accountNumber/transaction/";
    private static final String CREATE_INCOMING_TRANSACTION_LINK = "/account/:accountNumber/income/";
    private static final String GET_ALL_TRANSACTIONS_LINK = "/account/:accountNumber/transaction/";
    private static final String GET_TRANSACTION_LINK = "/account/:accountNumber/transaction/:transactionId";
    private static final String SETTLE_TRANSACTION_LINK = "/account/:accountNumber/transaction/:transactionId/settle";
    private static final String REJECT_TRANSACTION_LINK = "/account/:accountNumber/transaction/:transactionId/settle";
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

    public void start() {
        after((Filter) (request, response) -> response.type(CONTENT_TYPE));
        post(CREATE_ACCOUNT_LINK, CONTENT_TYPE, (req, res) -> {
            final CreateAccount createAccount = mapBody(req, CreateAccount.class);
            final Account account = createAccountUseCase.create(OwnerId.of(createAccount.getOwner()));
            res.status(201);
            return getAccountResponse(account);
        });
        get(GET_ACCOUNT_LINK, (req, res) -> {
            final AccountNumber accountNumber = getAccountNumber(req);
            final Account account = accountRepository.getBy(accountNumber);
            if (account == null) {
                res.status(404);
                return getMessage("Account Not Found");
            }
            return getAccountResponse(account);
        });
        post(CREATE_OUTGOING_TRANSACTION_LINK, CONTENT_TYPE, (req, res) -> {
            final AccountNumber accountNumber = getAccountNumber(req);
            final MakeOutgoingTransactionData makeOutgoingTransactionData = mapBody(req, MakeOutgoingTransactionData.class);
            final Transaction transaction = makeOutgoingTransaction(accountNumber, makeOutgoingTransactionData);
            res.status(201);
            return getTransactionStringResponse(transaction);
        });
        post(CREATE_INCOMING_TRANSACTION_LINK, CONTENT_TYPE, (req, res) -> {
            final AccountNumber accountNumber = getAccountNumber(req);
            final MakeIncomingTransactionData makeIncomingTransactionData = mapBody(req, MakeIncomingTransactionData.class);
            final Transaction transaction = makeIncomingTransaction(accountNumber, makeIncomingTransactionData);
            res.status(201);
            return getTransactionStringResponse(transaction);
        });
        get(GET_ALL_TRANSACTIONS_LINK, (req, res) -> {
            final AccountNumber accountNumber = getAccountNumber(req);
            if (!accountRepository.exist(accountNumber)) {
                res.status(404);
                return getMessage("Account Not Found");
            }
            final List<Transaction> transaction = transactionRepository.getAll(accountNumber);
            return getResponseForListOfTransactions(transaction.stream()
                    .map(this::getTransactionResponse)
                    .collect(toList()), accountNumber);
        });
        get(GET_TRANSACTION_LINK, (req, res) -> {
            final AccountNumber accountNumber = getAccountNumber(req);
            final TransactionId transactionId = getTransactionId(req);
            final Transaction transaction = transactionRepository.getBy(accountNumber, transactionId);

            if (transaction == null) {
                res.status(404);
                return getMessage("Transaction Not Found");
            }
            return getTransactionStringResponse(transaction);
        });
        post(SETTLE_TRANSACTION_LINK, CONTENT_TYPE, (req, res) -> {
            final AccountNumber accountNumber = getAccountNumber(req);
            final TransactionId transactionId = getTransactionId(req);
            final Transaction transaction = settleTransactionUseCase.settle(accountNumber, transactionId);
            return getTransactionStringResponse(transaction);
        });
        Spark.
        delete(REJECT_TRANSACTION_LINK, CONTENT_TYPE, (req, res) -> {
            final AccountNumber accountNumber = getAccountNumber(req);
            final TransactionId transactionId = getTransactionId(req);
            final Transaction transaction = rejectTransactionUseCase.reject(accountNumber, transactionId);
            return getTransactionStringResponse(transaction);
        });

        exception(MismatchedInputException.class, (e, request, response) -> {
            LOGGER.warn("Wrong user data", e);
            response.type(CONTENT_TYPE);
            response.body(getMessage("Wrong message"));
            response.status(HTTP_BAD_REQUEST);
        });
        exception(BusinessException.class, (e, request, response) -> {
            LOGGER.warn("Wrong user data", e);
            exceptionBodyWriter(e, response);
            response.status(HTTP_BAD_REQUEST);
        });
        exception(ValidationException.class, (e, request, response) -> {
            LOGGER.warn("Wrong user data validation", e);
            exceptionBodyWriter(e, response);
            response.status(HTTP_BAD_REQUEST);
        });
        exception(Exception.class, (e, request, response) -> {
            LOGGER.error("Error", e);
            exceptionBodyWriter(e, response);
            response.status(HTTP_INTERNAL_ERROR);
        });
    }

    private void exceptionBodyWriter(Exception e, spark.Response response) {
        response.type(CONTENT_TYPE);
        response.body(getMessage(e.getMessage()));
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

    private String getTransactionStringResponse(Transaction transaction) throws JsonProcessingException {
        final Response value = getTransactionResponse(transaction);
        return objectMapper.writeValueAsString(value);
    }

    private String getResponseForListOfTransactions(List<Response> transactions, AccountNumber accountNumber) throws JsonProcessingException {
        final ArrayList<Link> links = new ArrayList<>();
        links.add(new Link("GET", replace(GET_ACCOUNT_LINK, accountNumber), "account"));
        final Response response = new Response(transactions, links);
        return objectMapper.writeValueAsString(response);
    }

    private Response getTransactionResponse(Transaction transaction) {
        final TransactionData transactionData = new TransactionData(transaction);
        final ArrayList<Link> links = new ArrayList<>();
        links.add(new Link("GET", replace(GET_TRANSACTION_LINK, transaction), "self"));
        links.add(new Link("GET", replace(GET_ACCOUNT_LINK, transaction), "account"));
        if (transaction.transactionStatus() == TransactionStatus.CREATED) {
            links.add(new Link("POST", replace(SETTLE_TRANSACTION_LINK, transaction), "settle"));
            links.add(new Link("DELETE", replace(REJECT_TRANSACTION_LINK, transaction), "reject"));
        }
        return new Response(transactionData, links);
    }

    private String replace(String link, Transaction transaction) {
        return "http://localhost:" + Spark.port() +
                link.replace(":transactionId", transaction.transactionId().toString())
                        .replace(":accountNumber", transaction.transactionOwner().toString());
    }

    private String getAccountResponse(Account account) throws JsonProcessingException {
        final AccountData accountData = new AccountData(account);

        final ArrayList<Link> links = new ArrayList<>();
        links.add(new Link("GET", replace(GET_ACCOUNT_LINK, account), "self"));
        links.add(new Link("GET", replace(GET_ALL_TRANSACTIONS_LINK, account), "transactions"));
        links.add(new Link("POST", replace(CREATE_OUTGOING_TRANSACTION_LINK, account), "makeOutgoingTransaction"));
        links.add(new Link("POST", replace(CREATE_INCOMING_TRANSACTION_LINK, account), "receiveIncomingTransaction"));
        final Response value = new Response(accountData, links);
        return objectMapper.writeValueAsString(value);
    }

    private String replace(String link, Account account) {
        final AccountNumber accountNumber = account.accountNumber();
        return replace(link, accountNumber);
    }

    private String replace(String link, AccountNumber accountNumber) {
        return "http://localhost:" + Spark.port() +
                link.replace(":accountNumber", accountNumber.toString());
    }


}
