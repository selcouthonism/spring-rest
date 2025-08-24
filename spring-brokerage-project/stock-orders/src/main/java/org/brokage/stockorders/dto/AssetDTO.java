package org.brokage.stockorders.dto;


public record AssetDTO(
        Long id,
        Long customerId,

        String assetName,
        Long size,
        Long usableSize
) {}
