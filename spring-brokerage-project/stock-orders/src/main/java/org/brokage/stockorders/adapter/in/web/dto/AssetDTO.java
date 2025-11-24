package org.brokage.stockorders.adapter.in.web.dto;


import java.math.BigDecimal;

public record AssetDTO(
        Long id,
        Long customerId,

        String assetName,
        BigDecimal size,
        BigDecimal usableSize
) {}
