package org.brokage.stockorders.application.service;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.adapter.in.web.dto.CreateOrderDTO;
import org.brokage.stockorders.adapter.in.web.dto.OrderDTO;
import org.brokage.stockorders.adapter.in.web.mapper.WebOrderMapper;
import org.brokage.stockorders.application.port.out.OrderRepository;
import org.brokage.stockorders.domain.model.order.Order;
import org.brokage.stockorders.domain.service.OrderService;
import org.brokage.stockorders.domain.model.order.OrderStatus;
import org.brokage.stockorders.application.service.factory.OrderHandlerFactory;
import org.brokage.stockorders.application.service.handler.OrderAction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final OrderHandlerFactory orderHandlerFactory; // Injected factory
    private final WebOrderMapper apiMapper;

    /**
     * Create a new order with PENDING status.
     * If SELL -> check usableSize of asset and reduce it.
     */
    @Override
    @Transactional
    public OrderDTO create(CreateOrderDTO request) {
        Order domain = apiMapper.toDomain(request);

        Order order = (Order) orderHandlerFactory
                .getHandler(OrderAction.CREATE, request.orderSide())
                .handle(domain);

        return apiMapper.toDto(order);
    }

    /**
     * Find order with given orderId and customerId.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderDTO find(Long orderId, Long customerId) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, customerId);
        return apiMapper.toDto(order);
    }

    /**
     * List orders for customer within date range (optional filters).
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> list(Long customerId, Instant from, Instant to, OrderStatus orderStatus) {

        return orderRepository.findByFilter(customerId, from, to, orderStatus)
                .stream()
                .map(apiMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public OrderDTO cancel(Long orderId, Long customerId) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, customerId);

        order = (Order) orderHandlerFactory.getHandler(OrderAction.CANCEL, order.getOrderSide()).handle(order);

        return apiMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDTO matchOrder(Long orderId) {
        Order order = orderRepository.findById(orderId);
        order = (Order) orderHandlerFactory.getHandler(OrderAction.MATCH, order.getOrderSide()).handle(order);

        return apiMapper.toDto(order);
    }

}
