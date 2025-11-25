package org.brokage.stockorders.adapter.out.persistence.mapper;

import org.brokage.stockorders.adapter.out.persistence.entity.AssetEntity;
import org.brokage.stockorders.adapter.out.persistence.entity.CustomerEntity;
import org.brokage.stockorders.domain.model.asset.Asset;
import org.brokage.stockorders.domain.model.customer.Customer;
import org.springframework.stereotype.Component;

@Component
public class PersistenceAssetMapper {

    public AssetEntity toEntity(Asset asset) {
        AssetEntity entity = new AssetEntity();
        entity.setId(asset.getId());
        entity.setAssetName(asset.getAssetName());
        entity.setSize(asset.getSize());
        entity.setUsableSize(asset.getUsableSize());
        entity.setCreatedAt(asset.getCreatedAt());

        // convert customer domain → customer JPA reference
        entity.setCustomer(toCustomerReference(asset.getCustomer()));

        return entity;
    }

    private CustomerEntity toCustomerReference(Customer customer) {
        CustomerEntity ref = new CustomerEntity();
        ref.setId(customer.getId());
        return ref;
    }

    public AssetEntity mergeDomainIntoEntity(Asset asset, AssetEntity entity) {
        if(asset == null || entity == null) return null;

        // Only update mutable numeric fields
        // do NOT overwrite managed.customerId / assetName unless required
        //entity.setAssetName(asset.getAssetName());
        entity.setSize(asset.getSize());
        entity.setUsableSize(asset.getUsableSize());

        return entity;
    }

    public Asset toDomain(AssetEntity entity) {
        if (entity == null) return null;

        Asset domain = new Asset(
                toDomainCustomer(entity.getCustomer()),
                entity.getAssetName(),
                entity.getSize(),
                entity.getUsableSize()
        );
        domain.setId(entity.getId());
        domain.setCreatedAt(entity.getCreatedAt());

        // attach managed entity so updates can merge later
        domain.attachEntity(entity);

        return domain;
    }

    private Customer toDomainCustomer(CustomerEntity entity) {
        Customer customer = new Customer(entity.getId());
        customer.setFirstName(entity.getFirstName());
        customer.setLastName(entity.getLastName());
        customer.setPhoneNumber(entity.getPhoneNumber());
        customer.setEmail(entity.getEmail());
        customer.setCreateDate(entity.getCreateDate());

        return customer;
    }
}
