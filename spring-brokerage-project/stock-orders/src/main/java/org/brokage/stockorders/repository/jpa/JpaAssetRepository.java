package org.brokage.stockorders.repository.jpa;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.brokage.stockorders.model.entity.Asset;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaAssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

    Optional<Asset> findByAssetNameAndCustomerId(String assetName, Long customerId);

    // Find by customer and asset with pessimistic lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "javax.persistence.lock.timeout", value = "5000")
    })
    @Query("select a from Asset a where a.customer.id = :customerId and a.assetName = :assetName")
    Optional<Asset> findByCustomerIdAndAssetNameForUpdate(
            @Param("customerId") Long customerId,
            @Param("assetName") String assetName);
}
