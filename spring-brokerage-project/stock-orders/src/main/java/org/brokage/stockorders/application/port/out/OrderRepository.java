package org.brokage.stockorders.application.port.out;

import org.brokage.stockorders.adapter.out.persistence.entity.Order;

public interface OrderRepository {

    Order save(Order order);
    Order findById(Long orderId);
    Order findByIdAndCustomerId(Long orderId, Long customerId);
    Order findAllByCustomerId(Long customerId);
}
