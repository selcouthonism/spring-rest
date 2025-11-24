package org.brokage.stockorders.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.brokage.stockorders.domain.model.order.OrderSide;
import org.brokage.stockorders.domain.model.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderDTO(
        Long id,
        Long customerId,
        String assetName,
        OrderSide orderSide,
        BigDecimal size,
        BigDecimal price,
        OrderStatus status,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        Instant createDate
) {}
