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
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class OrderController {

    private final OrderService orderService;
    private final OrderModelAssembler assembler;

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    /**
     * Fetch a single order by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<OrderDTO>> getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principle) {

        log.info("Getting order with id {}", id);

        OrderDTO order = orderService.getOrder(id, principle.getId());
        return ResponseEntity.ok(assembler.toModel(order));
    }


    @PostMapping
    public ResponseEntity<EntityModel<OrderDTO>> createOrder(
            @Valid @RequestBody CreateOrderDTO request,
            @AuthenticationPrincipal CustomUserDetails principle) {

        log.info("Create order with CreateOrderDTO {}", request);

        OrderDTO savedOrder = orderService.createOrder(request, principle.getId());

        return ResponseEntity
                .created(linkTo(methodOn(OrderController.class).getOrder(savedOrder.id(), principle)).toUri())
                .body(assembler.toModel(savedOrder));
    }

    @GetMapping
    public ResponseEntity<List<EntityModel<OrderDTO>>> listOrders(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) OrderStatus orderStatus,
            @AuthenticationPrincipal CustomUserDetails principal) {

        List<OrderDTO> orders = orderService.listOrders(principal.getId(), from, to, orderStatus);

        List<EntityModel<OrderDTO>> models = orders.stream()
                .map(assembler::toModel)
                .toList();

        return ResponseEntity.ok(models);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<EntityModel<OrderDTO>> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails principle) {

        log.info("Cancel order for order {} for customer {}", orderId, principle.getId());
        OrderDTO canceledOrder = orderService.cancelOrder(orderId, principle.getId());
        return ResponseEntity.noContent().build();
    }
}
