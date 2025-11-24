package org.brokage.stockorders.service;

import org.brokage.stockorders.dto.CreateOrderDTO;
import org.brokage.stockorders.dto.OrderDTO;
import org.brokage.stockorders.model.enums.OrderStatus;

import java.time.Instant;
import java.util.List;


public interface OrderService {

    OrderDTO find(Long orderId, Long customerId);
    OrderDTO create(CreateOrderDTO request);
    OrderDTO cancel(Long orderId, Long customerId);
    List<OrderDTO> list(Long customerId, Instant from, Instant to, OrderStatus orderStatus);

    OrderDTO matchOrder(Long orderId); //Only admin
    boolean isOrderOwnedByCustomer(Long customerId, Long userId);
}

