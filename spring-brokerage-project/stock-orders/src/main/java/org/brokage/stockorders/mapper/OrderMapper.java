package org.brokage.stockorders.mapper;

import org.brokage.stockorders.dto.OrderDTO;
import org.brokage.stockorders.model.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "customer.id", target = "customerId")
    OrderDTO toDto(Order order);
}

/*
@Component
public class OrderMapper {

    public Order toDomain(CreateOrderDTO dto) {
        return new Order(
                dto.customerId(),
                dto.assetName(),
                dto.orderSide(),
                dto.size(),
                dto.price()
                );
    }

    public OrderDTO toDto(Order domain) {
        return new OrderDTO(
                domain.getId(),
                domain.getCustomerId(),
                domain.getAssetName(),
                domain.getOrderSide(),
                domain.getSize(),
                domain.getPrice(),
                domain.getStatus(),
                domain.getUpdateDate()
        );
    }
}
 */