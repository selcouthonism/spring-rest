package org.springapi.shopping.dto;

import org.springapi.shopping.enums.Status;

import java.time.Instant;

public class OrderDto {

    private Long id;
    private String product;
    private int quantity;
    private Status status;
    private Instant createdAt;
    private Instant updatedAt;

    public OrderDto() {}

    public OrderDto(Long id, String product, int quantity, Status status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
