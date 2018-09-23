package pl.slawek.restbank.infrastructure.endpoints.data;

import pl.slawek.restbank.domain.Transaction;
import pl.slawek.restbank.domain.TransactionStatus;

import java.math.BigDecimal;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class TransactionData {

    private final BigDecimal amount;
    private final String transactionId;
    private final String destination;
    private final String source;
    private final TransactionStatus transactionStatus;
    private final String dateTime;

    public TransactionData(Transaction transaction) {
        amount = transaction.amount().amount();
        transactionId = transaction.transactionId().toString();
        destination = transaction.destination().toString();
        source = transaction.source().toString();
        transactionStatus = transaction.transactionStatus();
        dateTime = ISO_OFFSET_DATE_TIME.format(transaction.modificationDate());
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getDestination() {
        return destination;
    }

    public String getSource() {
        return source;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public String getDateTime() {
        return dateTime;
    }
}
