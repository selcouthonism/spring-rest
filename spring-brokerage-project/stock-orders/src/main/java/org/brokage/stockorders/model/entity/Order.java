package org.brokage.stockorders.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.brokage.stockorders.model.enums.OrderSide;
import org.brokage.stockorders.model.enums.OrderStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders",
        indexes = {
                @Index(name = "idx_orders_customer_date", columnList = "customer_id, create_date DESC"),
                @Index(name = "idx_orders_status", columnList = "status"),
                @Index(name = "idx_orders_asset", columnList = "asset_name")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "asset_name", nullable = false, length = 50)
    private String assetName;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_side", nullable = false, length = 10)
    private OrderSide orderSide; // BUY or SELL

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status; // PENDING, MATCHED, CANCELED

    @CreationTimestamp
    @Column(name = "create_date", nullable = false, updatable = false)
    private Instant createDate;

    @UpdateTimestamp
    @Column(name = "update_date")
    private Instant updateDate;

    public Order(Customer customer, String assetName, OrderSide orderSide, Long size, BigDecimal price, OrderStatus status) {
        this.customer = customer;
        this.assetName = assetName;
        this.orderSide = orderSide;
        this.size = size;
        this.price = price;
        this.status = status;
    }
}
