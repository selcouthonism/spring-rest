package org.brokage.stockorders.mapper;

import org.brokage.stockorders.adapter.in.web.dto.OrderDTO;
import org.brokage.stockorders.domain.model.order.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "customer.id", target = "customerId")
    OrderDTO toDto(Order order);
}