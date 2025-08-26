package org.brokage.stockorders.service;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.exceptions.ResourceNotFoundException;
import org.brokage.stockorders.model.entity.Asset;
import org.brokage.stockorders.repository.AssetRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssetFinder {

    private final AssetRepository assetRepository;

    public Asset findAssetForCustomerOrThrow(Long customerId, String assetName) {
        return assetRepository.findByCustomerIdAndAssetNameForUpdate(customerId, assetName)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for customer: " + customerId + ", assetName: " + assetName));
    }

}
