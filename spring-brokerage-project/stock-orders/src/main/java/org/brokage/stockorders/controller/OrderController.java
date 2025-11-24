package org.brokage.stockorders.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.assembler.OrderModelAssembler;
import org.brokage.stockorders.dto.CreateOrderDTO;
import org.brokage.stockorders.dto.OrderDTO;
import org.brokage.stockorders.model.enums.OrderStatus;
import org.brokage.stockorders.service.OrderService;
import org.brokage.stockorders.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') || (hasRole('CUSTOMER') && #customerId == #principal.customerId)")
public class OrderController {

    private final OrderService orderService;
    private final OrderModelAssembler assembler;

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);


    @PostMapping
    public ResponseEntity<EntityModel<OrderDTO>> createOrder(
            @PathVariable Long customerId,
            @Valid @RequestBody CreateOrderDTO request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        log.info("Create order with CreateOrderDTO {}", request);

        OrderDTO savedOrder = orderService.createOrder(request);

        return ResponseEntity
                .created(linkTo(methodOn(OrderController.class).getOrder(customerId, savedOrder.id(), principal)).toUri())
                .body(assembler.toModel(savedOrder));
    }


    /**
     * Fetch a single order by ID.
     */
    @GetMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN') || (hasRole('CUSTOMER') && #customerId == #principal.customerId) && @orderAuthorization.canAccessCustomer(#id, #principal.customerId)")
    public ResponseEntity<EntityModel<OrderDTO>> getOrder(
            @PathVariable Long customerId,
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        log.info("Getting order with id {}", id);

        OrderDTO order = orderService.getOrder(id, customerId);
        return ResponseEntity.ok(assembler.toModel(order));
    }

    @GetMapping
    public ResponseEntity<List<EntityModel<OrderDTO>>> listOrders(
            @PathVariable Long customerId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) OrderStatus orderStatus,
            @AuthenticationPrincipal CustomUserDetails principal) {

        List<OrderDTO> orders = orderService.listOrders(customerId, from, to, orderStatus);

        List<EntityModel<OrderDTO>> models = orders.stream()
                .map(assembler::toModel)
                .toList();

        return ResponseEntity.ok(models);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EntityModel<OrderDTO>> cancelOrder(
            @PathVariable Long customerId,
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        log.info("Cancel order for order {} for customer {}", id, customerId);
        OrderDTO canceledOrder = orderService.cancelOrder(id, customerId);
        return ResponseEntity.noContent().build();
    }
}
