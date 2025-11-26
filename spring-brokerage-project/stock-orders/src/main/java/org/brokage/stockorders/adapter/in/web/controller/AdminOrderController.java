package org.brokage.stockorders.adapter.in.web.controller;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.adapter.in.web.assembler.OrderModelAssembler;
import org.brokage.stockorders.adapter.in.web.dto.OrderDTO;
import org.brokage.stockorders.security.CustomUserDetails;
import org.brokage.stockorders.application.port.in.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;
    private final OrderModelAssembler assembler;

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    /**
     * Update orderStatus PENDING -> MATCHED
     */
    @PutMapping("/{id}/match")
    public ResponseEntity<EntityModel<OrderDTO>> matchOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        log.info("Match order with id {}", id);

        OrderDTO order = orderService.matchOrder(id);
        return ResponseEntity.ok(assembler.toModel(order));
    }
}
