package org.brokage.stockorders.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.brokage.stockorders.domain.model.order.OrderSide;
import org.brokage.stockorders.domain.model.order.OrderStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

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
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Column(name = "asset_name", nullable = false, length = 50)
    private String assetName;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_side", nullable = false, length = 10)
    private OrderSide orderSide; // BUY or SELL

    @Column(nullable = false)
    private BigDecimal size;

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

    private OrderEntity(CustomerEntity customer, String assetName, OrderSide orderSide, BigDecimal size, BigDecimal price) {
        this.customer = customer;
        this.assetName = assetName;
        this.orderSide = orderSide;
        this.size = size;
        this.price = price;
        this.status = OrderStatus.PENDING;
    }

    public static OrderEntity create(CustomerEntity customer, String assetName, OrderSide orderSide, BigDecimal size, BigDecimal price){
        OrderEntity order = new OrderEntity();
        order.setCustomer(customer);
        order.setAssetName(assetName);
        order.setOrderSide(orderSide);
        order.setSize(size);
        order.setPrice(price);
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(Instant.now());
        order.setUpdateDate(Instant.now());

        return order;
    }
}
