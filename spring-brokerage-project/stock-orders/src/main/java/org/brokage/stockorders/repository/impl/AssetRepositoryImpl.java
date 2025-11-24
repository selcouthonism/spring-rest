package org.brokage.stockorders.repository.impl;

import org.brokage.stockorders.exceptions.ResourceNotFoundException;
import org.brokage.stockorders.model.entity.Asset;
import org.brokage.stockorders.model.entity.Customer;
import org.brokage.stockorders.repository.AssetRepository;
import org.brokage.stockorders.repository.jpa.AssetJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AssetRepositoryImpl implements AssetRepository {

    private final AssetJpaRepository assetJpaRepository;

    public AssetRepositoryImpl(AssetJpaRepository assetJpaRepository) {
        this.assetJpaRepository = assetJpaRepository;
    }

    @Override
    public Asset findCustomerAsset(String assetName, Long customerId) {
        return assetJpaRepository.findByAssetNameAndCustomerId(assetName, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for customer: " + customerId + ", assetName: " + assetName));
    }

    @Override
    public Asset lockAssetForCustomer( String assetName, Long customerId) {
        return assetJpaRepository.findByCustomerIdAndAssetNameForUpdate(customerId, assetName)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for customer: " + customerId + ", assetName: " + assetName));
    }

    @Override
    public Asset findOrCreateAssetForUpdate(String assetName, Customer customer) {
        return assetJpaRepository.findByCustomerIdAndAssetNameForUpdate(customer.getId(), assetName)
                .orElseGet(() -> assetJpaRepository.save(
                new Asset(customer, assetName, BigDecimal.ZERO, BigDecimal.ZERO
                )
        ));
    }

    @Override
    public Asset save(Asset asset) {
        return assetJpaRepository.save(asset);
    }
}
