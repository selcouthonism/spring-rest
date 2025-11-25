package org.brokage.stockorders.domain.service;

import org.brokage.stockorders.adapter.in.web.dto.CreateOrderDTO;
import org.brokage.stockorders.adapter.in.web.dto.OrderDTO;
import org.brokage.stockorders.domain.model.order.OrderStatus;

import java.time.Instant;
import java.util.List;


public interface OrderService {

    OrderDTO find(Long orderId, Long customerId);
    OrderDTO create(CreateOrderDTO request);
    OrderDTO cancel(Long orderId, Long customerId);
    List<OrderDTO> list(Long customerId, Instant from, Instant to, OrderStatus orderStatus);

    OrderDTO matchOrder(Long orderId); //Only admin
}

