package org.brokerage.orders.domain.model;

import java.math.BigDecimal;

public class Order {
    private Long id;
    private Long customerId;
    private String assetSymbol;
    private BigDecimal size;
    private String status; // PENDING, COMPLETED, CANCELLED

    // getters/setters/constructors omitted for brevity

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public void markCancelled() {
        this.status = "CANCELLED";
    }

    public void markMatched() {
        this.status = "MATCHED";
    }
}
