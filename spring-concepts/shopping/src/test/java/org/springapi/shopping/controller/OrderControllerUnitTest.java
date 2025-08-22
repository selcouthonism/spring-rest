package org.springapi.shopping.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springapi.shopping.assembler.OrderModelAssembler;
import org.springapi.shopping.dto.OrderDto;
import org.springapi.shopping.enums.Status;
import org.springapi.shopping.model.Order;
import org.springapi.shopping.service.OrderService;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderControllerUnitTest {

    @InjectMocks
    private OrderController controller;

    @Mock
    private OrderService service;

    @Mock
    private OrderModelAssembler assembler;

    private Order order;
    private OrderDto dto;
    private EntityModel<OrderDto> entityModel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        order = new Order("Test Product", 5);
        order.setId(1L);
        order.setStatus(Status.IN_PROGRESS);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        dto = new OrderDto(
                order.getId(),
                order.getProduct(),
                order.getQuantity(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );

        entityModel = EntityModel.of(dto);
    }

    @Test
    void testGetOrderById() {
        when(service.getOrderById(1L)).thenReturn(order);
        when(assembler.toModel(order)).thenReturn(entityModel);

        EntityModel<OrderDto> response = controller.getOrderById(1L);

        assertNotNull(response);
        assertEquals(dto, response.getContent());
        verify(service).getOrderById(1L);
        verify(assembler).toModel(order);
    }

    @Test
    void testCreateOrder() {
        when(service.createOrder(any(Order.class))).thenReturn(order);
        when(assembler.toModel(order)).thenReturn(entityModel);

        ResponseEntity<EntityModel<OrderDto>> response = controller.newOrder(dto);

        assertNotNull(response);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(entityModel, response.getBody());

        verify(service).createOrder(any(Order.class));
        verify(assembler).toModel(order);
    }

    @Test
    void testCancelOrder() {
        when(service.cancelOrder(1L)).thenReturn(order);
        when(assembler.toModel(order)).thenReturn(entityModel);

        ResponseEntity<EntityModel<OrderDto>> response = controller.cancel(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(entityModel, response.getBody());

        verify(service).cancelOrder(1L);
        verify(assembler).toModel(order);
    }

    @Test
    void testCompleteOrder() {
        when(service.completeOrder(1L)).thenReturn(order);
        when(assembler.toModel(order)).thenReturn(entityModel);

        ResponseEntity<EntityModel<OrderDto>> response = controller.complete(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(entityModel, response.getBody());

        verify(service).completeOrder(1L);
        verify(assembler).toModel(order);
    }
}