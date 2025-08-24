package org.brokage.stockorders.repository;

import org.brokage.stockorders.model.entity.Asset;
import org.brokage.stockorders.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

    List<Asset> findByCustomer(Customer customer);

    /**
     * Finds a specific asset for a given customer.
     * Crucial for validating SELL orders.
     *
     * @param customer the customer who owns the asset.
     * @param assetName the name of the asset (e.g., "AAPL").
     * @return an Optional containing the asset if it exists.
     */
    Optional<Asset> findByCustomerAndAssetName(Customer customer, String assetName);
}
