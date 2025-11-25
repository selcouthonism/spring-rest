package org.brokage.stockorders.adapter.out.persistence.repository;

import org.brokage.stockorders.adapter.out.persistence.entity.AssetEntity;
import org.brokage.stockorders.adapter.out.persistence.entity.CustomerEntity;
import org.brokage.stockorders.adapter.out.persistence.jpa.JpaCustomerRepository;
import org.brokage.stockorders.adapter.out.persistence.mapper.PersistenceAssetMapper;
import org.brokage.stockorders.adapter.out.persistence.specification.AssetSpecifications;
import org.brokage.stockorders.application.exception.ResourceNotFoundException;
import org.brokage.stockorders.application.port.out.AssetRepository;
import org.brokage.stockorders.adapter.out.persistence.jpa.JpaAssetRepository;
import org.brokage.stockorders.domain.model.asset.Asset;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class AssetRepositoryImpl implements AssetRepository {

    private final JpaAssetRepository jpaAssetRepository;
    private final JpaCustomerRepository jpaCustomerRepository;
    private final PersistenceAssetMapper mapper;

    public AssetRepositoryImpl(JpaAssetRepository jpaAssetRepository, JpaCustomerRepository jpaCustomerRepository, PersistenceAssetMapper mapper) {
        this.jpaAssetRepository = jpaAssetRepository;
        this.jpaCustomerRepository = jpaCustomerRepository;
        this.mapper = mapper;
    }

    @Override
    public Asset findById(Long assetId) {
        AssetEntity entity = jpaAssetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found."));

        return mapper.toDomain(entity);
    }

    @Override
    public Asset findByIdAndCustomerId(Long assetId, Long customerId) {
        AssetEntity entity = jpaAssetRepository.findByIdAndCustomerId(assetId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found."));

        return mapper.toDomain(entity);
    }

    @Override
    public List<Asset> findAllByFilter(Long customerId, String  assetName) {
        Specification<AssetEntity> spec = Specification.allOf(
                AssetSpecifications.hasCustomerId(customerId),
                AssetSpecifications.hasAssetName(assetName)
        );

        return jpaAssetRepository.findAll(spec).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Asset save(Asset asset) {
        //Update existing entity
        AssetEntity entity = asset.getEntityForUpdate();
        if(entity != null){
            mapper.mergeDomainIntoEntity(asset, entity);  // update fields only
            return mapper.toDomain(entity);
        }

        //Create new entity
        entity = mapper.toEntity(asset);
        AssetEntity savedEntity = jpaAssetRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Asset lockAssetForCustomer( String assetName, Long customerId) {
        AssetEntity entity = jpaAssetRepository.findByCustomerIdAndAssetNameForUpdate(customerId, assetName)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for customer: " + customerId + ", assetName: " + assetName));

        return mapper.toDomain(entity);
    }

    //This method is created for easy test. Not good!!!
    @Override
    public Asset findOrCreateAssetForUpdate(String assetName, Long customerId) {
        // Try to lock the existing asset
        Optional<AssetEntity> locked = jpaAssetRepository
                .findByCustomerIdAndAssetNameForUpdate(customerId, assetName);

        if (locked.isPresent()) {
            return mapper.toDomain(locked.get());
        }

        // Asset does not exist → create one safely
        CustomerEntity customerEntity = jpaCustomerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));

        AssetEntity newAsset = new AssetEntity(
                customerEntity,
                assetName,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        return mapper.toDomain(jpaAssetRepository.save(newAsset));
    }

}
