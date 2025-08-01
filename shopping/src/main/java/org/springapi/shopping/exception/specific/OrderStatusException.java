package org.springapi.shopping.exception.specific;


import org.springapi.shopping.exception.general.AppException;
import org.springapi.shopping.model.Order;
import org.springframework.http.HttpStatus;

public class OrderStatusException extends AppException {
    private final Order order;
    private final String action;

    public OrderStatusException(Order order, String action) {
        super("You can't " + action + " an order that is in the " + order.getStatus() + " status", "Method not allowed", HttpStatus.METHOD_NOT_ALLOWED);
        this.order = order;
        this.action = action;
    }

    public Order getOrder() {
        return order;
    }

    public String getAction() {
        return action;
    }
}
