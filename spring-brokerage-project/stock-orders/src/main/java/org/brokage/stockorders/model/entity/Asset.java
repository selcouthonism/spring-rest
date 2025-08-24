package org.brokage.stockorders.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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

    @Column(nullable = false)
    private Long size; // total shares

    @Column(nullable = false)
    private Long usableSize; // available shares not locked in PENDING SELL orders

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @Version
    private Long version; // optimistic locking ensures concurrency control (important for SELL reservations).


    public Asset(Customer customer, String assetName, Long size, Long usableSize) {
        this.customer = customer;
        this.assetName = assetName;
        this.size = size;
        this.usableSize = usableSize;
    }
}
