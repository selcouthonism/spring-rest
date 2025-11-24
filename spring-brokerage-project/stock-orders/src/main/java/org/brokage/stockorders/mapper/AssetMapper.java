package org.brokage.stockorders.mapper;

import org.brokage.stockorders.adapter.in.web.dto.AssetDTO;
import org.brokage.stockorders.adapter.out.persistence.entity.Asset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssetMapper {

    @Mapping(source = "customer.id", target = "customerId")
    AssetDTO toDto(Asset asset);
}
