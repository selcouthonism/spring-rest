package org.brokage.stockorders.service.impl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.dto.CreateOrderDTO;
import org.brokage.stockorders.dto.OrderDTO;
import org.brokage.stockorders.exceptions.OperationNotPermittedException;
import org.brokage.stockorders.exceptions.UnallowedAccessException;
import org.brokage.stockorders.mapper.OrderMapper;
import org.brokage.stockorders.repository.OrderSpecifications;
import org.brokage.stockorders.service.OrderService;
import org.brokage.stockorders.model.entity.Customer;
import org.brokage.stockorders.repository.CustomerRepository;
import org.brokage.stockorders.model.entity.Order;
import org.brokage.stockorders.repository.OrderRepository;
import org.brokage.stockorders.model.enums.OrderSide;
import org.brokage.stockorders.model.enums.OrderStatus;
import org.brokage.stockorders.exceptions.ResourceNotFoundException;
import org.brokage.stockorders.service.factory.OrderHandlerFactory;
import org.brokage.stockorders.service.handler.OrderAction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderMapper orderMapper;

    private final OrderHandlerFactory orderHandlerFactory; // Injected factory

    /**
     * Create a new order with PENDING status.
     * If SELL -> check usableSize of asset and reduce it.
     */
    @Override
    @Transactional
    public OrderDTO createOrder(CreateOrderDTO request) {
        return createOrder(request, request.customerId());
    }

    @Override
    @Transactional
    public OrderDTO createOrder(CreateOrderDTO request, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        validateOrder(request, customer);

        OrderSide orderSide = OrderSide.valueOf(request.orderSide().toUpperCase());

        orderHandlerFactory.getHandler(OrderAction.CREATE, orderSide).handle(request);

        Order newOrder = Order.create(customer, request.assetName(), orderSide, request.size(), request.price());
        Order savedOrder = orderRepository.save(newOrder);
        return orderMapper.toDto(savedOrder);
    }

    /**
     * Find order with given orderId.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrder(Long orderId) {
        return getOrder(orderId, null);
    }

    /**
     * Find order with given orderId and customerId.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrder(Long orderId, Long customerId) {
        Order order = findOrderById(orderId);
        if(customerId != null) validateCustomerAccess(order, customerId);
        return orderMapper.toDto(order);
    }

    /**
     * List orders for customer within date range (optional filters).
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> listOrders(Long customerId, Instant from, Instant to, OrderStatus orderStatus) {

        //allOf(...) → AND
        //anyOf(...) → OR
        Specification<Order> spec = Specification.allOf(
                OrderSpecifications.createdAfter(from),
                OrderSpecifications.createdBefore(to),
                OrderSpecifications.hasCustomerId(customerId),
                OrderSpecifications.hasStatus(orderStatus)
        );

        return orderRepository.findAll(spec).stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public OrderDTO cancelOrder(Long orderId) {
        return cancelOrder(orderId, null);
    }

    @Override
    @Transactional
    public OrderDTO cancelOrder(Long orderId, Long customerId) {
        Order order = findOrderById(orderId);
        if(customerId != null) validateCustomerAccess(order, customerId);

        orderHandlerFactory.getHandler(OrderAction.CANCEL, order.getOrderSide()).handle(order);

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDTO matchOrder(Long orderId) {
        Order order = findOrderById(orderId);
        orderHandlerFactory.getHandler(OrderAction.MATCH, order.getOrderSide()).handle(order);

        order.setStatus(OrderStatus.MATCHED);
        orderRepository.save(order);

        return orderMapper.toDto(order);
    }

    //Utility
    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for ID:" + orderId));
    }

    private void validateOrder(CreateOrderDTO request, Customer customer) {
        // Security Check: Ensure the customer owns this order
        if (!request.customerId().equals(customer.getId())) {
            throw new ValidationException("Invalid customer id.");
        }

        if(request.assetName().equals("TRY")) {
            throw new OperationNotPermittedException("Cannot buy or sell TRY assets.");
        }

        if (request.size().compareTo(BigDecimal.ZERO) <= 0 || request.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Order size and price must be positive.");
        }
    }

    private void validateCustomerAccess(Order order, Long customerId) {
        // Security Check: Ensure the customer owns this order
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new UnallowedAccessException("Order customer not permitted");
        }
    }

}
