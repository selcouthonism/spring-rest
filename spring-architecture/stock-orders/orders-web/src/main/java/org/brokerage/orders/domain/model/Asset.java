package org.brokerage.orders.domain.model;

import java.math.BigDecimal;

public class Asset {

    private Long id;
    private Long customerId;
    private String asset; // e.g. AAPL, TRY
    private BigDecimal usableSize;
    private BigDecimal size;

    public void reserve(BigDecimal amount) {
        this.usableSize = this.usableSize.subtract(amount);
    }

    public void release(BigDecimal amount) {
        this.usableSize = this.usableSize.add(amount);
    }

    // getters/setters omitted
}
