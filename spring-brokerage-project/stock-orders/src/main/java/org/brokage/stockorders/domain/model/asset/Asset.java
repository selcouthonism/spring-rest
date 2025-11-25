package org.brokage.stockorders.domain.model.asset;

import lombok.Getter;
import lombok.Setter;
import org.brokage.stockorders.adapter.out.persistence.entity.AssetEntity;
import org.brokage.stockorders.domain.exception.NotEnoughBalanceException;
import org.brokage.stockorders.domain.model.customer.Customer;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class Asset {
    private Long id;
    private Customer customer;
    private String assetName;
    private BigDecimal size; // total shares
    private BigDecimal usableSize; // available shares not locked in PENDING SELL orders
    private Instant createdAt;

    private transient AssetEntity persistenceRef;   // <--- IMPORTANT

    public void attachEntity(AssetEntity entity) {
        this.persistenceRef = entity;
    }

    public AssetEntity getEntityForUpdate() {
        return persistenceRef;
    }

    public Asset() {}

    public Asset(Customer customer, String assetName, BigDecimal size, BigDecimal usableSize) {
        this.customer = customer;
        this.assetName = assetName;
        this.size = size;
        this.usableSize = usableSize;
    }

    public void releaseFunds(BigDecimal amount){
        this.usableSize = usableSize.add(amount);
    }

    public void withdrawFromUsable(BigDecimal amount){
        this.usableSize = usableSize.subtract(amount);
    }

    public void withdrawFromSize(BigDecimal amount){
        this.size = size.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        this.size = this.size.add(amount);
        this.usableSize = this.usableSize.add(amount);
    }

    public void checkUsableSize(BigDecimal requestedAmount){
        if (getUsableSize().compareTo(requestedAmount) < 0) {
            throw new NotEnoughBalanceException("Insufficient usable shares for asset: " + assetName
                    + "Have: " + usableSize + ", Need: " + requestedAmount );
        }
    }
}
