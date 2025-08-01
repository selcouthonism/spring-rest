package org.springapi.shopping.mapper;

import org.springapi.shopping.dto.OrderDto;
import org.springapi.shopping.model.Order;

public class OrderMapper {

    public static OrderDto toDto(Order order) {
        return new OrderDto(
                order.getId(),
                order.getProduct(),
                order.getQuantity(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public static Order toEntity(OrderDto dto) {
        Order order = new Order();
        order.setId(dto.getId());
        order.setProduct(dto.getProduct());
        order.setQuantity(dto.getQuantity());
        order.setStatus(dto.getStatus());
        // createdAt/updatedAt should not be set from client input
        return order;
    }

}
