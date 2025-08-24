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