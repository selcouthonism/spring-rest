package org.springapi.shopping.exception;


import org.springapi.shopping.model.Order;

public class OrderStatusException extends RuntimeException {
    private final Order order;
    private final String action;

    public OrderStatusException(Order order, String action) {
        super("Cannot " + action + " order with status: " + order.getStatus());
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
