package org.springapi.shopping.controller;

import org.springapi.shopping.assembler.OrderModelAssembler;
import org.springapi.shopping.dto.OrderDto;
import org.springapi.shopping.mapper.OrderMapper;
import org.springapi.shopping.model.Order;
import org.springapi.shopping.service.OrderService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;
    private final OrderModelAssembler assembler;

    public OrderController(OrderService service, OrderModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping("/{id}")
    public EntityModel<OrderDto> getOrderById(@PathVariable Long id) {
        Order order = service.getOrderById(id);
        return assembler.toModel(order);
    }

    @GetMapping
    public CollectionModel<EntityModel<OrderDto>> getAllOrders() {

        List<Order> orders = service.getAllOrders();

        /*
        if (orders.isEmpty()) {
            return CollectionModel.of(List.of(),
                    linkTo(methodOn(OrderController.class).getAllOrders()).withSelfRel());
        }
         */

        List<EntityModel<OrderDto>> models = orders.stream()
                .map(assembler::toModel)
                .toList();

        return CollectionModel.of(models,
                linkTo(methodOn(OrderController.class).getAllOrders()).withSelfRel());
    }

    @PostMapping
    ResponseEntity<EntityModel<OrderDto>> newOrder(@RequestBody OrderDto orderDto) {
        Order savedOrder = service.createOrder(OrderMapper.toEntity(orderDto));

        return ResponseEntity //
                .created(linkTo(methodOn(OrderController.class).getOrderById(savedOrder.getId())).toUri()) //
                .body(assembler.toModel(savedOrder));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<EntityModel<OrderDto>> cancel(@PathVariable Long id) {
        Order order = service.cancelOrder(id);

        return ResponseEntity.ok(assembler.toModel(order));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<EntityModel<OrderDto>> complete(@PathVariable Long id) {

        Order order = service.complete(id);

        return ResponseEntity.ok(assembler.toModel(order));
    }

}

