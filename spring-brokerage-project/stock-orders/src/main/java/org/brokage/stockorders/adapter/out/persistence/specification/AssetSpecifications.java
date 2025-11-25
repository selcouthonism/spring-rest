package org.brokage.stockorders.adapter.out.persistence.specification;

import org.brokage.stockorders.adapter.out.persistence.entity.AssetEntity;
import org.springframework.data.jpa.domain.Specification;

public class AssetSpecifications {

    public static Specification<AssetEntity> hasCustomerId(Long customerId) {
        return (root, query, cb) -> customerId != null
                ? cb.equal(root.get("customer").get("id"), customerId)
                : null;
    }

    public static Specification<AssetEntity> hasAssetName(String assetName) {
        return (root, query, cb) -> assetName != null
                ? cb.equal(root.get("assetName"), assetName)
                : null;
    }
}
