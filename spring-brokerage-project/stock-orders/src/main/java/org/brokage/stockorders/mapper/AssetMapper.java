package org.brokage.stockorders.mapper;

import org.brokage.stockorders.dto.AssetDTO;
import org.brokage.stockorders.model.entity.Asset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssetMapper {

    @Mapping(source = "customer.id", target = "customerId")
    AssetDTO toDto(Asset asset);
}
