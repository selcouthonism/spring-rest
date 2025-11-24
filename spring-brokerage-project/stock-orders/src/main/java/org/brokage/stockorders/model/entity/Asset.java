package org.brokage.stockorders.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "assets",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"customer_id", "asset_name"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "asset_name", nullable = false, length = 50)
    private String assetName;

    @DecimalMin(value = "0.0", inclusive = true, message = "Size must be non-negative")
    @Column(nullable = false)
    private BigDecimal size; // total shares

    @DecimalMin(value = "0.0", inclusive = true, message = "Usable size must be non-negative")
    @Column(nullable = false)
    private BigDecimal usableSize; // available shares not locked in PENDING SELL orders

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @Version
    private long version; // optimistic locking ensures concurrency control (important for SELL reservations).

    public Asset(Customer customer, String assetName, BigDecimal size, BigDecimal usableSize) {
        this.customer = customer;
        this.assetName = assetName;
        this.size = size;
        this.usableSize = usableSize;
    }

    // Custom validation for usableSize <= size
    @AssertTrue(message = "Usable size must be less than or equal to size")
    public boolean isUsableSizeValid() {
        return usableSize == null || size == null || usableSize.compareTo(size) <= 0;
    }

    public void releaseFunds(BigDecimal amount){
        setUsableSize(getUsableSize().add(amount));
    }
}
