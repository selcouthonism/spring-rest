package org.brokage.stockorders.application.service.handler;

import org.brokage.stockorders.domain.model.order.OrderSide;

/**
 * A generic strategy for a specific action on a specific order side.
 * We use a generic 'context' object to pass data to avoid long method signatures.
 */
public interface OrderActionHandler<T> {

    T handle(T context);

    OrderAction getAction();

    OrderSide getOrderSide();
}
