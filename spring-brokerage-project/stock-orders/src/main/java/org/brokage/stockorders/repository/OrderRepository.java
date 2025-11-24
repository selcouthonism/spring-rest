package org.brokage.stockorders.repository;

import org.brokage.stockorders.model.entity.Order;

public interface OrderRepository {

    Order save(Order order);
    Order findById(Long orderId);
    Order findByIdAndCustomerId(Long orderId, Long customerId);
    Order findAllByCustomerId(Long customerId);
}
