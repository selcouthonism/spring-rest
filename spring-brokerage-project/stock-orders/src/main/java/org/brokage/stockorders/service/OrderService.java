package org.brokage.stockorders.service;

import org.brokage.stockorders.dto.CreateOrderDTO;
import org.brokage.stockorders.dto.OrderDTO;
import org.brokage.stockorders.model.enums.OrderStatus;

import java.time.Instant;
import java.util.List;


public interface OrderService {

    OrderDTO getOrder(Long orderId, Long customerId);

    OrderDTO createOrder(CreateOrderDTO request);

    OrderDTO cancelOrder(Long orderId, Long customerId);

    List<OrderDTO> listOrders(Long customerId, Instant from, Instant to, OrderStatus orderStatus);

    OrderDTO matchOrder(Long orderId); //Only admin

    boolean isOrderOwnedByCustomer(Long customerId, Long userId);
}

