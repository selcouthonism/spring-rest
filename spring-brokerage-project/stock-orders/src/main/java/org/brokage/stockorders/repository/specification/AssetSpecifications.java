package org.brokage.stockorders.repository.specification;

import org.brokage.stockorders.model.entity.Asset;
import org.springframework.data.jpa.domain.Specification;

public class AssetSpecifications {

    public static Specification<Asset> hasCustomerId(Long customerId) {
        return (root, query, cb) -> customerId != null
                ? cb.equal(root.get("customer").get("id"), customerId)
                : null;
    }

    public static Specification<Asset> hasAssetName(String assetName) {
        return (root, query, cb) -> assetName != null
                ? cb.equal(root.get("assetName"), assetName)
                : null;
    }
}
