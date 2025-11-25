package org.brokage.stockorders.application.port.out;


import org.brokage.stockorders.domain.model.order.Order;
import org.brokage.stockorders.domain.model.order.OrderStatus;

import java.time.Instant;
import java.util.List;

public interface OrderRepository {

    Order save(Order order);
    Order findById(Long orderId);
    Order findByIdAndCustomerId(Long orderId, Long customerId);
    List<Order> findByFilter(Long customerId, Instant from, Instant to, OrderStatus orderStatus);
}
