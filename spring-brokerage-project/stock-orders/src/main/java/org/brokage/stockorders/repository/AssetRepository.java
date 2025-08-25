package org.brokage.stockorders.repository;

import jakarta.persistence.LockModeType;
import org.brokage.stockorders.model.entity.Asset;
import org.brokage.stockorders.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

    List<Asset> findByCustomer(Customer customer);

    // Find by customer and asset with pessimistic lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Asset a where a.customer.id = :customerId and a.assetName = :assetName")
    Optional<Asset> findByCustomerIdAndAssetNameForUpdate(
            @Param("customerId") Long customerId,
            @Param("assetName") String assetName);
}
