package org.brokage.stockorders.service;

import org.brokage.stockorders.dto.CreateOrderDTO;
import org.brokage.stockorders.dto.OrderDTO;

import java.util.List;

public interface CustomerOrderService {

    public OrderDTO create(CreateOrderDTO request);
    public OrderDTO find(Long customerId);
    public List<OrderDTO> list(Long customerId);
    public OrderDTO cancel(Long customerId);
}
