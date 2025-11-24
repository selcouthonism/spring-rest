package org.brokage.stockorders.service.impl;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.dto.CreateOrderDTO;
import org.brokage.stockorders.dto.OrderDTO;
import org.brokage.stockorders.exceptions.UnallowedAccessException;
import org.brokage.stockorders.mapper.OrderMapper;
import org.brokage.stockorders.repository.CustomerRepository;
import org.brokage.stockorders.repository.specification.OrderSpecifications;
import org.brokage.stockorders.service.OrderService;
import org.brokage.stockorders.model.entity.Customer;
import org.brokage.stockorders.model.entity.Order;
import org.brokage.stockorders.repository.jpa.OrderJpaRepository;
import org.brokage.stockorders.model.enums.OrderSide;
import org.brokage.stockorders.model.enums.OrderStatus;
import org.brokage.stockorders.exceptions.ResourceNotFoundException;
import org.brokage.stockorders.service.factory.OrderHandlerFactory;
import org.brokage.stockorders.service.handler.OrderAction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderMapper orderMapper;
    private final CustomerRepository  customerRepository;

    private final OrderHandlerFactory orderHandlerFactory; // Injected factory

    /**
     * Create a new order with PENDING status.
     * If SELL -> check usableSize of asset and reduce it.
     */
    @Override
    @Transactional
    public OrderDTO create(CreateOrderDTO request) {
        final Long customerId = request.customerId();

        Customer customer = customerRepository.findByIdOrThrow(customerId);

        OrderSide orderSide = OrderSide.valueOf(request.orderSide().toUpperCase());
        //OrderSide orderSide = request.orderSide();
        orderHandlerFactory.getHandler(OrderAction.CREATE, orderSide).handle(request);

        Order newOrder = Order.create(customer, request.assetName(), orderSide, request.size(), request.price());
        Order savedOrder = orderJpaRepository.save(newOrder);
        return orderMapper.toDto(savedOrder);
    }

    /**
     * Find order with given orderId and customerId.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderDTO find(Long orderId, Long customerId) {
        Order order = findOrderById(orderId);
        if(customerId != null) validateCustomerAccess(order, customerId);
        return orderMapper.toDto(order);
    }

    /**
     * List orders for customer within date range (optional filters).
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> list(Long customerId, Instant from, Instant to, OrderStatus orderStatus) {

        //allOf(...) → AND
        //anyOf(...) → OR
        Specification<Order> spec = Specification.allOf(
                OrderSpecifications.createdAfter(from),
                OrderSpecifications.createdBefore(to),
                OrderSpecifications.hasCustomerId(customerId),
                OrderSpecifications.hasStatus(orderStatus)
        );

        return orderJpaRepository.findAll(spec).stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public OrderDTO cancel(Long orderId, Long customerId) {
        Order order = findOrderById(orderId);
        if(customerId != null) validateCustomerAccess(order, customerId);

        orderHandlerFactory.getHandler(OrderAction.CANCEL, order.getOrderSide()).handle(order);

        order.setStatus(OrderStatus.CANCELED);
        orderJpaRepository.save(order);

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDTO matchOrder(Long orderId) {
        Order order = findOrderById(orderId);
        orderHandlerFactory.getHandler(OrderAction.MATCH, order.getOrderSide()).handle(order);

        order.setStatus(OrderStatus.MATCHED);
        orderJpaRepository.save(order);

        return orderMapper.toDto(order);
    }

    //Utility
    private Order findOrderById(Long orderId) {
        return orderJpaRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for ID:" + orderId));
    }

    // Note: The following method only implemented for demo purposes.
    // The exception message gives information about system and creates a vulnerability.
    private void validateCustomerAccess(Order order, Long customerId) {
        // Security Check: Ensure the customer owns this order
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new UnallowedAccessException("Order customer not permitted");
        }
    }

    public boolean isOrderOwnedByCustomer(Long orderId, Long customerId) {
        return orderJpaRepository.existsByIdAndCustomerId(orderId, customerId);
    }

}
