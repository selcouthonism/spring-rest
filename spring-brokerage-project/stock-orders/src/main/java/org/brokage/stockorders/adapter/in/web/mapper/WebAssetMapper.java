package org.brokage.stockorders.adapter.in.web.mapper;

import org.brokage.stockorders.adapter.in.web.dto.AssetDTO;
import org.brokage.stockorders.domain.model.asset.Asset;
import org.brokage.stockorders.domain.model.customer.Customer;
import org.springframework.stereotype.Component;

@Component
public class WebAssetMapper {

    public Asset toDomain(AssetDTO dto) {
        Asset asset = new Asset(
                new Customer(dto.customerId()),
                dto.assetName(),
                dto.size(),
                dto.usableSize()
        );
        asset.setId(dto.id());

        return asset;

    }

    public AssetDTO toDto(Asset domain) {
        return new AssetDTO(
                domain.getId(),
                domain.getCustomer().getId(),
                domain.getAssetName(),
                domain.getSize(),
                domain.getUsableSize()
        );
    }
}
