package org.brokage.stockorders.adapter.in.web.assembler;

import org.brokage.stockorders.adapter.in.web.controller.OrderController;
import org.brokage.stockorders.adapter.in.web.dto.OrderDTO;
import org.brokage.stockorders.domain.model.order.OrderStatus;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class OrderModelAssembler implements RepresentationModelAssembler<OrderDTO, EntityModel<OrderDTO>> {

    @Override
    public EntityModel<OrderDTO> toModel(OrderDTO dto) {
        EntityModel<OrderDTO> model = EntityModel.of(dto);

        // self link
        Link self = linkTo(methodOn(OrderController.class)
                .getOrder(null, dto.id(), null)) // null will be injected for @AuthenticationPrincipal
                .withSelfRel();
        model.add(self);

        // list link
        Link list = linkTo(methodOn(OrderController.class)
                .listOrders(null, null, null, null, null)) // date range params can be null for link building
                .withRel("orders");
        model.add(list);

        // conditional cancel link
        if (OrderStatus.PENDING.equals(dto.status())) {
            Link cancel = linkTo(methodOn(OrderController.class).cancelOrder(null, dto.id(), null)).withRel("cancel");
            model.add(cancel);
        }

        return model;
    }
}
