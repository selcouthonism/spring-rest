package org.springapi.shopping.service;

import org.springapi.shopping.enums.Status;
import org.springapi.shopping.exception.OrderNotFoundException;
import org.springapi.shopping.exception.OrderStatusException;
import org.springapi.shopping.model.Order;
import org.springapi.shopping.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> getAllOrders(){
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id) //
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public Order createOrder(Order order) {
        order.setStatus(Status.IN_PROGRESS);

        return orderRepository.save(order);
    }

    public Order cancelOrder(Long id) {
        Order order = orderRepository.findById(id) //
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() != Status.IN_PROGRESS) {
            throw new OrderStatusException(order, "cancel");
        }

        order.setStatus(Status.CANCELLED);

        return  orderRepository.save(order);
    }

    public Order complete(Long id) {
        Order order = orderRepository.findById(id) //
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() != Status.IN_PROGRESS) {
            throw new OrderStatusException(order, "complete");
        }

        order.setStatus(Status.COMPLETED);

        return orderRepository.save(order);
    }
}
