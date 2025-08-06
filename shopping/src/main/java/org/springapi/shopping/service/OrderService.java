package org.springapi.shopping.service;

import org.springapi.shopping.enums.Status;
import org.springapi.shopping.exception.specific.OrderNotFoundException;
import org.springapi.shopping.exception.specific.OrderStatusException;
import org.springapi.shopping.model.Order;
import org.springapi.shopping.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
//@RequiredArgsConstructor // automatically generate a constructor for a class with all the final fields
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Fetch all orders from the database.
     */
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Retrieve a specific order by ID.
     */
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return getOrderOrThrow(id);
    }

    /**
     * Create a new order and set its status to IN_PROGRESS.
     */
    @Transactional
    public Order createOrder(Order order) {
        order.setStatus(Status.IN_PROGRESS);
        return orderRepository.save(order);
    }

    /**
     * Cancel an order only if it's in IN_PROGRESS status.
     */
    @Transactional
    public Order cancelOrder(Long id) {
        Order order = getOrderOrThrow(id);
        validateOrderStatus(order, Status.IN_PROGRESS, "cancel");
        order.setStatus(Status.CANCELLED);
        return orderRepository.save(order);
    }

    /**
     * Complete an order only if it's in IN_PROGRESS status.
     */
    @Transactional
    public Order completeOrder(Long id) {
        Order order = getOrderOrThrow(id);
        validateOrderStatus(order, Status.IN_PROGRESS, "complete");
        order.setStatus(Status.COMPLETED);
        return orderRepository.save(order);
    }

    /**
     * Internal helper to get an order or throw if not found.
     */
    private Order getOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    /**
     * Validate order is in expected status before proceeding.
     */
    private void validateOrderStatus(Order order, Status expectedStatus, String action) {
        if (order.getStatus() != expectedStatus) {
            throw new OrderStatusException(order, action);
        }
    }
}