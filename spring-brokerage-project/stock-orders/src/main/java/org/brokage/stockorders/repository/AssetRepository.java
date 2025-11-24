package org.brokage.stockorders.repository;

import org.brokage.stockorders.model.entity.Asset;
import org.brokage.stockorders.model.entity.Customer;

public interface AssetRepository {
    Asset save(Asset asset);
    Asset findCustomerAsset(String assetName, Long customerId);
    Asset lockAssetForCustomer(String assetName, Long customerId);
    Asset findOrCreateAssetForUpdate(String assetName, Customer customer);
}
