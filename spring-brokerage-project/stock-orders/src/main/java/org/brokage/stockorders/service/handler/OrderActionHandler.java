package org.brokage.stockorders.service.handler;

import org.brokage.stockorders.model.enums.OrderSide;

/**
 * A generic strategy for a specific action on a specific order side.
 * We use a generic 'context' object to pass data to avoid long method signatures.
 */
public interface OrderActionHandler<T> {

    void handle(T context);

    OrderAction getAction();

    OrderSide getOrderSide();
}
