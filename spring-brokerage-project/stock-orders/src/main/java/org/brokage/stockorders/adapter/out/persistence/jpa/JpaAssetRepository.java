package org.brokage.stockorders.adapter.out.persistence.jpa;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.brokage.stockorders.adapter.out.persistence.entity.AssetEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaAssetRepository extends JpaRepository<AssetEntity, Long>, JpaSpecificationExecutor<AssetEntity> {

    Optional<AssetEntity> findByIdAndCustomerId(Long assetId, Long customerId);

    // Find by customer and asset with pessimistic lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "javax.persistence.lock.timeout", value = "5000")
    })
    @Query("select a from AssetEntity a where a.customer.id = :customerId and a.assetName = :assetName")
    Optional<AssetEntity> findByCustomerIdAndAssetNameForUpdate(
            @Param("customerId") Long customerId,
            @Param("assetName") String assetName);
}
