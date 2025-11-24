package org.brokage.stockorders.application.port.out;

import org.brokage.stockorders.adapter.out.persistence.entity.Asset;
import org.brokage.stockorders.adapter.out.persistence.entity.Customer;

public interface AssetRepository {
    Asset save(Asset asset);
    Asset findCustomerAsset(String assetName, Long customerId);
    Asset lockAssetForCustomer(String assetName, Long customerId);
    Asset findOrCreateAssetForUpdate(String assetName, Customer customer);
}
