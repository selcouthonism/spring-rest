package org.brokage.stockorders.adapter.in.web.mapper;

import org.brokage.stockorders.adapter.in.web.dto.CreateOrderDTO;
import org.brokage.stockorders.adapter.in.web.dto.OrderDTO;
import org.brokage.stockorders.domain.model.order.Order;

public class WebUserOrderMapper {
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
