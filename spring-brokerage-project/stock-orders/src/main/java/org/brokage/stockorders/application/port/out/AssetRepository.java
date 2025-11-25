package org.brokage.stockorders.application.port.out;

import org.brokage.stockorders.domain.model.asset.Asset;

import java.util.List;

public interface AssetRepository {
    Asset findById(Long assetId);
    Asset findByIdAndCustomerId(Long assetId, Long customerId);
    List<Asset> findAllByFilter(Long customerId, String assetName);
    Asset save(Asset asset);

    // Persistence-level operations for locking/upsert that return attached entity info.
    Asset lockAssetForCustomer(String assetName, Long customerId);
    Asset findOrCreateAssetForUpdate(String assetName, Long customerId);
}
