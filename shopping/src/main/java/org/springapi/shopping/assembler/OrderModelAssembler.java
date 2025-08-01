package org.springapi.shopping.assembler;

import org.springapi.shopping.controller.OrderController;
import org.springapi.shopping.dto.OrderDto;
import org.springapi.shopping.enums.Status;
import org.springapi.shopping.mapper.OrderMapper;
import org.springapi.shopping.model.Order;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class OrderModelAssembler implements RepresentationModelAssembler<Order, EntityModel<OrderDto>> {

    @Override
    public EntityModel<OrderDto> toModel(Order order) {
        OrderDto dto = OrderMapper.toDto(order);

        EntityModel<OrderDto> model = EntityModel.of(dto,
                linkTo(methodOn(OrderController.class).getOrderById(order.getId())).withSelfRel(),
                linkTo(methodOn(OrderController.class).getAllOrders()).withRel("orders")
        );

        if (order.getStatus() == Status.IN_PROGRESS) {
            model.add(linkTo(methodOn(OrderController.class).complete(order.getId())).withRel("complete"));
            model.add(linkTo(methodOn(OrderController.class).cancel(order.getId())).withRel("cancel"));
        }

        return model;
    }
}
