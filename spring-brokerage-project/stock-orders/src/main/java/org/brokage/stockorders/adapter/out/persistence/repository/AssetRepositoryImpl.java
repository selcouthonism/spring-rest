package org.brokage.stockorders.adapter.out.persistence.repository;

import org.brokage.stockorders.application.exception.ResourceNotFoundException;
import org.brokage.stockorders.adapter.out.persistence.entity.Asset;
import org.brokage.stockorders.adapter.out.persistence.entity.Customer;
import org.brokage.stockorders.application.port.out.AssetRepository;
import org.brokage.stockorders.adapter.out.persistence.jpa.JpaAssetRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AssetRepositoryImpl implements AssetRepository {

    private final JpaAssetRepository jpaAssetRepository;

    public AssetRepositoryImpl(JpaAssetRepository jpaAssetRepository) {
        this.jpaAssetRepository = jpaAssetRepository;
    }

    @Override
    public Asset findCustomerAsset(String assetName, Long customerId) {
        return jpaAssetRepository.findByAssetNameAndCustomerId(assetName, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for customer: " + customerId + ", assetName: " + assetName));
    }

    @Override
    public Asset lockAssetForCustomer( String assetName, Long customerId) {
        return jpaAssetRepository.findByCustomerIdAndAssetNameForUpdate(customerId, assetName)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for customer: " + customerId + ", assetName: " + assetName));
    }

    @Override
    public Asset findOrCreateAssetForUpdate(String assetName, Customer customer) {
        return jpaAssetRepository.findByCustomerIdAndAssetNameForUpdate(customer.getId(), assetName)
                .orElseGet(() -> jpaAssetRepository.save(
                new Asset(customer, assetName, BigDecimal.ZERO, BigDecimal.ZERO
                )
        ));
    }

    @Override
    public Asset save(Asset asset) {
        return jpaAssetRepository.save(asset);
    }
}
