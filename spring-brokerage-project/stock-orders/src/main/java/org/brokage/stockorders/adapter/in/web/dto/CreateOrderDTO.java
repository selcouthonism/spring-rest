package org.brokage.stockorders.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import org.brokage.stockorders.domain.model.order.OrderSide;
import org.brokage.stockorders.utility.ValidEnum;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateOrderDTO(
        @NotNull(message = "CustomerId is required")
        Long customerId,

        @NotBlank(message = "Asset name is required")
        String assetName,

        @NotNull(message = "Order side must be specified")
        @ValidEnum(enumClass = OrderSide.class, message = "Order side must be BUY or SELL")
        //OrderSide orderSide,
        String orderSide,

        @NotNull(message = "Size is required")
        @Positive(message = "Size must be greater than zero")
        BigDecimal size,

        @NotNull(message = "Price cannot be null")
        @Positive(message = "Price must be greater than zero")
        //@DecimalMin(value = "0.01", message = "Price must be positive") // Stock price might be pretty small like 0.00001
        BigDecimal price
) {}
