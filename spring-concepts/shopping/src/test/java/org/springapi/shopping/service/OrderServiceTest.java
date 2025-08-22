package org.springapi.shopping.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springapi.shopping.enums.Status;
import org.springapi.shopping.exception.specific.OrderNotFoundException;
import org.springapi.shopping.exception.specific.OrderStatusException;
import org.springapi.shopping.model.Order;
import org.springapi.shopping.repository.OrderRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getOrderById_shouldReturnOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(Status.IN_PROGRESS);

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertEquals(order, result);
    }

    @Test
    void getOrderById_shouldThrow_whenNotFound() {
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(1L));
    }

    @Test
    void cancelOrder_shouldUpdateStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(Status.IN_PROGRESS);

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Mockito.when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.cancelOrder(1L);

        assertEquals(Status.CANCELLED, result.getStatus());
    }

    @Test
    void cancelOrder_shouldThrow_whenInvalidStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(Status.COMPLETED);

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(OrderStatusException.class, () -> orderService.cancelOrder(1L));
    }
}