package org.brokage.stockorders.service.factory;

import jakarta.validation.ValidationException;
import org.brokage.stockorders.model.enums.OrderSide;
import org.brokage.stockorders.service.handler.OrderAction;
import org.brokage.stockorders.service.handler.OrderActionHandler;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderHandlerFactory {

    // A 2D Map: Map<Action, Map<Side, Handler>>
    private final Map<OrderAction, Map<OrderSide, OrderActionHandler>> handlerMap;

    //write-once, read-many
    public OrderHandlerFactory(List<OrderActionHandler> handlers) {
        handlerMap = new EnumMap<>(OrderAction.class);
        // Initialize the outer map keys
        for (OrderAction action : OrderAction.values()) {
            handlerMap.put(action, new EnumMap<>(OrderSide.class));
        }
        // Populate the map with all the handler beans found by Spring
        handlers.forEach(handler ->
                handlerMap.get(handler.getAction()).put(handler.getOrderSide(), handler)
        );
    }

    public OrderActionHandler getHandler(OrderAction action, OrderSide side) {
        Map<OrderSide, OrderActionHandler> sideHandlers = handlerMap.get(action);
        if (sideHandlers == null) {
            throw new ValidationException("No handlers found for action: " + action);
        }
        OrderActionHandler handler = sideHandlers.get(side);
        if (handler == null) {
            throw new ValidationException("No handler for action " + action + " and side " + side);
        }
        return handler;
    }
}
