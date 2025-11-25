package org.brokage.stockorders.domain.model.order;

import jakarta.validation.ValidationException;
import lombok.Getter;
import lombok.Setter;
import org.brokage.stockorders.application.exception.OperationNotPermittedException;
import org.brokage.stockorders.domain.model.customer.Customer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Getter
@Setter
public class Order {

    private Long id;
    private Customer customer;
    private String assetName;
    private OrderSide orderSide;
    private BigDecimal size;
    private BigDecimal price;
    private OrderStatus status;
    private Instant createDate;
    private Instant updateDate;

    public Order() {}
    public Order(Customer customer, String assetName, OrderSide orderSide, BigDecimal size, BigDecimal price) {
        this.customer = customer;
        this.assetName = assetName;
        this.orderSide = orderSide;
        this.size = size;
        this.price = price;
        this.status = OrderStatus.PENDING;
        this.createDate = Instant.now();
        this.updateDate = Instant.now();
    }

    public void validate(){
        checkAssetName();
    }

    private void checkAssetName(){
        if(assetName.equals("TRY")) {
            throw new OperationNotPermittedException("Cannot buy or sell TRY assets.");
        }
    }

    private void checkOrderSize(){
        if (size.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Invalid order size.");
        }
    }

    private void checkOrderPrice(){
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Invalid order price.");
        }
    }

    //Business logic
    public BigDecimal getTotalCost(){
        BigDecimal requiredAmount = getSize().multiply(getPrice()).setScale(2, RoundingMode.HALF_UP);

        if (requiredAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Order amount is not positive");
        }

        return requiredAmount;
    }

    public void cancel() {
        if (this.status != OrderStatus.PENDING) {
            throw new OperationNotPermittedException("Cannot cancel a non-pending order. Status is:" + status);
        }
        this.status = OrderStatus.CANCELED;
    }

    public void match() {
        if (this.status != OrderStatus.PENDING) {
            throw new OperationNotPermittedException("Cannot match a non-pending order. Status is:" + status);
        }
        this.status = OrderStatus.MATCHED;
    }
}
