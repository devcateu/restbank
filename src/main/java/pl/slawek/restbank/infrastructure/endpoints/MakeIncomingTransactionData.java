package pl.slawek.restbank.infrastructure.endpoints;

import java.math.BigDecimal;

public class MakeIncomingTransactionData {

    private String source;
    private BigDecimal amount;

    public String getSource() {
        return source;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
