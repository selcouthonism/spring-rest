package org.springapi.shopping.model;

import jakarta.persistence.*;
import org.springapi.shopping.enums.Status;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "CUSTOMER_ORDER")
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String product;

    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.IN_PROGRESS;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private int version;

    // Constructors
    public Order() {}

    public Order(String product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.status = Status.IN_PROGRESS;
    }

    // Optional convenience constructor (e.g., for tests)
    public Order(Long id, String product, int quantity, Status status) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.status = status != null ? status : Status.IN_PROGRESS;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setProduct(String product) { this.product = product; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setStatus(Status status) { this.status = status; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
